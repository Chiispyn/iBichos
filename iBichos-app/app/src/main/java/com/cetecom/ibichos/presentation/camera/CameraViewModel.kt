package com.cetecom.ibichos.presentation.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.BuildConfig
import com.cetecom.ibichos.data.api.KindwiseApi
import com.cetecom.ibichos.data.api.KindwiseRequest
import com.cetecom.ibichos.data.repository.CloudinaryApi
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.capture.CaptureResult
import com.cetecom.ibichos.domain.usecase.capture.ProcessCaptureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

sealed interface CameraUiState {
    object Idle : CameraUiState
    object Loading : CameraUiState
    data class Preview(val bitmap: Bitmap, val lat: Double?, val lon: Double?) : CameraUiState
    data class Success(val message: String) : CameraUiState
    data class Error(val message: String) : CameraUiState
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val kindwiseApi: KindwiseApi,
    private val cloudinaryApi: CloudinaryApi,
    private val processCaptureUseCase: ProcessCaptureUseCase
) : ViewModel() {

    private val cloudName    = "drubfka1z"
    private val uploadPreset = "IBichos"

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun setLoading()  { _uiState.value = CameraUiState.Loading }
    fun setPreview(bitmap: Bitmap, lat: Double?, lon: Double?) { _uiState.value = CameraUiState.Preview(bitmap, lat, lon) }
    fun setError(message: String)  { _uiState.value = CameraUiState.Error(message) }
    fun resetState()  { _uiState.value = CameraUiState.Idle }

    fun processCapture(bitmap: Bitmap, lat: Double?, lon: Double?, context: Context) {
        val uid = authRepository.getCurrentUserId() ?: run {
            _uiState.value = CameraUiState.Error("No hay usuario autenticado")
            return
        }

        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading
            runCatching {
                // 1. Guardar bitmap en archivo temporal
                val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }

                // 2. Subir a Cloudinary
                val filePart   = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/*".toMediaType()))
                val presetBody = uploadPreset.toRequestBody("text/plain".toMediaType())
                val imageUrl   = cloudinaryApi.uploadImage(cloudName, filePart, presetBody).secure_url
                    ?: throw Exception("Cloudinary no devolvió una URL válida")

                // 3. Codificar bitmap a Base64 para Kindwise
                val baos   = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                // 4. Identificar insecto con Kindwise
                val response   = kindwiseApi.identifyInsect(
                    apiKey  = BuildConfig.KINDWISE_API_KEY,
                    request = KindwiseRequest(listOf("data:image/jpeg;base64,$base64"), lat, lon)
                )
                val suggestion = response.result?.classification?.suggestions?.firstOrNull()
                    ?: throw Exception("No se identificó ningún insecto. Intenta con otra imagen.")

                val scientificName = suggestion.name ?: "Desconocido"
                val commonName     = suggestion.details?.common_names?.firstOrNull() ?: ""
                val displayName    = if (commonName.isNotEmpty()) commonName.uppercase() else scientificName.uppercase()
                val probability    = suggestion.probability ?: 0.0
                val description    = suggestion.details?.description?.value
                    ?: "Insecto registrado taxonómicamente por la IA Kindwise."

                val category    = inferCategoryFromName(scientificName)
                val dangerLevel = inferDangerLevel(category)

                // 5. Delegar lógica de negocio al UseCase
                processCaptureUseCase(
                    uid            = uid,
                    imageUrl       = imageUrl,
                    insectName     = displayName,
                    scientificName = scientificName,
                    category       = category,
                    dangerLevel    = dangerLevel,
                    probability    = probability,
                    lat            = lat,
                    lon            = lon,
                    description    = description
                )
            }
                .onSuccess { result ->
                    _uiState.value = when (result) {
                        is CaptureResult.Success ->
                            CameraUiState.Success(result.message)
                        is CaptureResult.LowConfidence ->
                            CameraUiState.Error(
                                "🔍 La IA no pudo identificar un insecto con suficiente certeza " +
                                "(${(result.probability * 100).toInt()}%). Intenta con otra foto con mejor iluminación."
                            )
                    }
                }
                .onFailure { e ->
                    _uiState.value = CameraUiState.Error("Error: ${e.message}")
                }
        }
    }

    private fun inferCategoryFromName(scientificName: String): InsectCategory {
        val lower = scientificName.lowercase()
        return when {
            lower.contains("araneae")     || lower.contains("latrodectus") ||
            lower.contains("loxosceles") || lower.contains("scorpion")    -> InsectCategory.ARACHNID
            lower.contains("coleoptera") || lower.contains("coccinella")  ||
            lower.contains("carabus")    || lower.contains("dynastes")    -> InsectCategory.COLEOPTERA
            lower.contains("lepidoptera")|| lower.contains("papilio")     ||
            lower.contains("danaus")     || lower.contains("morpho")      -> InsectCategory.LEPIDOPTERA
            lower.contains("hymenoptera")|| lower.contains("apis")        ||
            lower.contains("bombus")     || lower.contains("vespula")     ||
            lower.contains("formica")                                      -> InsectCategory.HYMENOPTERA
            else -> InsectCategory.OTHER
        }
    }

    private fun inferDangerLevel(category: InsectCategory): DangerLevel = when (category) {
        InsectCategory.ARACHNID    -> DangerLevel.VENOMOUS
        InsectCategory.LEPIDOPTERA -> DangerLevel.HARMLESS
        InsectCategory.COLEOPTERA  -> DangerLevel.HARMLESS
        InsectCategory.HYMENOPTERA -> DangerLevel.CAUTION
        else                       -> DangerLevel.UNKNOWN
    }
}
