package com.cetecom.ibichos.presentation.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.BuildConfig
import com.cetecom.ibichos.data.api.KindwiseApi
import com.cetecom.ibichos.data.api.KindwiseRequest
import com.cetecom.ibichos.data.repository.CaptureRepositoryImpl
import com.cetecom.ibichos.data.repository.CloudinaryModule
import com.cetecom.ibichos.data.repository.EventRepositoryImpl
import com.cetecom.ibichos.data.repository.UserRepositoryImpl
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.model.enums.GamificationConfig
import com.cetecom.ibichos.domain.model.enums.MedalInfo
import com.google.firebase.auth.FirebaseAuth
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

sealed interface CameraUiState {
    object Idle : CameraUiState
    object Loading : CameraUiState
    data class Preview(val bitmap: Bitmap, val lat: Double?, val lon: Double?) : CameraUiState
    data class Success(val message: String) : CameraUiState
    data class Error(val message: String) : CameraUiState
}

class CameraViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val captureRepository = CaptureRepositoryImpl()
    private val userRepository = UserRepositoryImpl()
    private val eventRepository = EventRepositoryImpl()
    private val api = KindwiseApi.create()

    // Cloudinary
    private val cloudName = "drubfka1z"
    private val uploadPreset = "IBichos"

    // ── MODO EMULADOR ─────────────────────────────────────────────────────────
    private val isEmulatorMode = true
    // ─────────────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun setLoading() {
        _uiState.value = CameraUiState.Loading
    }

    fun setPreview(bitmap: Bitmap, lat: Double?, lon: Double?) {
        _uiState.value = CameraUiState.Preview(bitmap, lat, lon)
    }

    fun processCapture(bitmap: Bitmap, lat: Double?, lon: Double?, context: Context) {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = CameraUiState.Error("No hay usuario autenticado")
            return
        }

        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading

            try {
                // 1. Guardar imagen temporalmente en archivo
                val filename = UUID.randomUUID().toString() + ".jpg"
                val file = File(context.cacheDir, filename)

                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
                    fos.flush()
                }

                if (isEmulatorMode) {
                    saveCaptureData(
                        uid = uid,
                        imageUrl = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg", // URL falsa de prueba
                        insectName = "ABEJA",
                        scientificName = "Apis mellifera",
                        category = InsectCategory.HYMENOPTERA,
                        dangerLevel = DangerLevel.CAUTION,
                        probability = 0.99,
                        lat = lat,
                        lon = lon,
                        description = "La abeja europea (Apis mellifera) es una especie de himenóptero apócrito de la familia Apidae. Es la especie de abeja con mayor distribución en el mundo. Son insectos polinizadores cruciales para la supervivencia de ecosistemas y para la polinización cruzada de la agricultura mundial."
                    )
                } else {
                    // 2. Subir archivo a Cloudinary
                    val requestFile = file.asRequestBody("image/*".toMediaType())

                    val filePart = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        requestFile
                    )

                    val presetBody = uploadPreset.toRequestBody("text/plain".toMediaType())

                    val cloudinaryResponse = CloudinaryModule.api.uploadImage(
                        cloudName = cloudName,
                        file = filePart,
                        uploadPreset = presetBody
                    )

                    val cloudinaryImageUrl = cloudinaryResponse.secure_url
                        ?: throw Exception("Cloudinary no devolvió una URL válida")

                    // 3. Convertir bitmap a Base64 para Kindwise
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                    // 4. Identificar insecto
                    val response = api.identifyInsect(
                        apiKey = BuildConfig.KINDWISE_API_KEY,
                        request = KindwiseRequest(
                            images = listOf("data:image/jpeg;base64,$base64"),
                            latitude = lat,
                            longitude = lon
                        )
                    )

                    val suggestion = response.result?.classification?.suggestions?.firstOrNull()
                    if (suggestion == null) {
                        _uiState.value = CameraUiState.Error(
                            "No se identificó ningún insecto. Intenta con otra imagen."
                        )
                        return@launch
                    }

                    val scientificName = suggestion.name ?: "Desconocido"
                    val commonName = suggestion.details?.common_names?.firstOrNull() ?: ""
                    val displayName = if (commonName.isNotEmpty()) {
                        commonName.uppercase()
                    } else {
                        scientificName.uppercase()
                    }
                    val prob = suggestion.probability ?: 0.0
                    val description =
                        suggestion.details?.description?.value
                            ?: "Insecto registrado taxonómicamente por la IA Kindwise. Se recomienda realizar un cruce de datos con enciclopedias especializadas para confirmar hábitos alimenticios y zona biológica nativa. Mantener respeto por el ecosistema local."

                    // 5. Determinar categoría taxonómica desde el nombre científico
                    //    (En el futuro se puede usar el campo 'taxon' de la respuesta de Kindwise)
                    val category = inferCategoryFromName(scientificName)

                    // 6. Peligro: Matriz de riesgo real basada en categoría taxonómica
                    val dangerLevel = when (category) {
                        InsectCategory.ARACHNID -> DangerLevel.VENOMOUS // Arañas/Escorpiones
                        InsectCategory.LEPIDOPTERA -> DangerLevel.HARMLESS // Mariposas
                        InsectCategory.COLEOPTERA -> DangerLevel.HARMLESS // Escarabajos
                        InsectCategory.HYMENOPTERA -> DangerLevel.CAUTION  // Abejas/Avispas/Hormigas
                        else -> DangerLevel.UNKNOWN
                    }

                    saveCaptureData(
                        uid = uid,
                        imageUrl = cloudinaryImageUrl,
                        insectName = displayName,
                        scientificName = scientificName,
                        category = category,
                        dangerLevel = dangerLevel,
                        probability = prob,
                        lat = lat,
                        lon = lon,
                        description = description
                    )
                }

            } catch (e: Exception) {
                _uiState.value = CameraUiState.Error("Error: ${e.message}")
            }
        }
    }

    private suspend fun saveCaptureData(
        uid: String,
        imageUrl: String,
        insectName: String,
        scientificName: String,
        category: InsectCategory,
        dangerLevel: DangerLevel,
        probability: Double,
        lat: Double?,
        lon: Double?,
        description: String
    ) {
        // ── Moderación automática ─────────────────────────────────────────────
        // Si la IA tiene < 40% de certeza: rechazar y sumar strike
        // Si tiene entre 40% y 75%: aceptar pero marcar para revisión humana
        // Si tiene >= 75%: aprobar automáticamente
        val needsReview: Boolean
        val status: String

        // Calcular UNA sola vez antes de guardar nada
        val isNewInsect = !captureRepository.hasCaughtInsect(uid, scientificName)
        val xpGain = if (isNewInsect) GamificationConfig.XP_NEW_SPECIES else GamificationConfig.XP_REPEATED_SPECIES

        when {
            probability < 0.40 -> {
                // Rechazada: no dar XP
                _uiState.value = CameraUiState.Error(
                    "🔍 La IA no pudo identificar un insecto con suficiente certeza (${(probability * 100).toInt()}%). Intenta con otra foto con mejor iluminación."
                )
                return
            }
            probability < 0.75 -> {
                // Aceptada pero pendiente de revisión humana
                needsReview = true
                status = "PENDING_REVIEW"
            }
            else -> {
                // Aprobada automáticamente
                needsReview = false
                status = "APPROVED"
            }
        }

        captureRepository.saveCapture(
            userId = uid,
            imageUrl = imageUrl,
            insectName = insectName,
            scientificName = scientificName,
            category = category,
            dangerLevel = dangerLevel,
            probability = probability,
            latitude = lat,
            longitude = lon,
            xpAwarded = xpGain,
            description = description,
            needsReview = needsReview,
            status = status
        )

        // No dar XP si el usuario está shadowbanned (se verifica en incrementXp del repositorio)
        userRepository.incrementXp(uid, xpGain)

        // ── Calcular medallas (Lógica centralizada en GamificationConfig) ───
        val currentUser = userRepository.getUserProfile(uid)
        val medalsToUnlock = GamificationConfig.evaluateMedalsToUnlock(
            currentUser = currentUser,
            isNewInsect = isNewInsect,
            dangerLevel = dangerLevel,
            category = category
        )

        // 📝 Log completo de SPECIES_DISCOVERED con nombre (antes de unlockMedals)
        if (isNewInsect) {
            runCatching {
                eventRepository.logSpeciesDiscovered(
                    userId = uid,
                    scientificName = scientificName,
                    insectName = insectName,
                    category = category.name,
                    xpAtEvent = currentUser.gamification.xp + xpGain
                )
            }
        }

        userRepository.unlockMedalsAndIncrementUnique(
            uid,
            medalsToUnlock,
            isNewInsect,
            category.name  // Ahora siempre es el nombre del enum en inglés (ej: "ARACHNID")
        )

        // ── Mensaje de éxito ──────────────────────────────────────────────────
        val noveltyMsg = if (isNewInsect) "✨ ¡NUEVA ESPECIE DESCUBIERTA! ✨\n" else ""
        val reviewMsg  = if (needsReview) "\n⚠️ Pendiente de verificación" else ""
        val medalsMsg  = if (medalsToUnlock.isNotEmpty()) {
            "\n🏅 Logros: ${medalsToUnlock.joinToString { MedalInfo.fromId(it)?.title ?: it }}"
        } else ""

        _uiState.value = CameraUiState.Success(
            "${noveltyMsg}🦟 ¡Insecto Atrapado!\n$insectName\nConfianza: ${(probability * 100).toInt()}%\n🎁 +$xpGain XP$medalsMsg$reviewMsg"
        )
    }

    /**
     * Infiere la categoría del insecto a partir de su nombre científico.
     * En el futuro se puede usar el campo 'taxon.order' de la respuesta de Kindwise.
     */
    private fun inferCategoryFromName(scientificName: String): InsectCategory {
        val lower = scientificName.lowercase()
        return when {
            // Órdenes de arácnidos (no insectos, pero incluidos en el scope de la app)
            lower.contains("araneae") || lower.contains("latrodectus") ||
            lower.contains("loxosceles") || lower.contains("scorpion") -> InsectCategory.ARACHNID

            // Coleoptera
            lower.contains("coleoptera") || lower.contains("coccinella") ||
            lower.contains("carabus") || lower.contains("dynastes") -> InsectCategory.COLEOPTERA

            // Lepidoptera
            lower.contains("lepidoptera") || lower.contains("papilio") ||
            lower.contains("danaus") || lower.contains("morpho") -> InsectCategory.LEPIDOPTERA

            // Hymenoptera (abejas, avispas, hormigas)
            lower.contains("hymenoptera") || lower.contains("apis") ||
            lower.contains("bombus") || lower.contains("vespula") ||
            lower.contains("formica") -> InsectCategory.HYMENOPTERA

            else -> InsectCategory.OTHER
        }
    }

    /** Traduce el ID de medalla a nombre para mostrar en la UI. */
    private fun medalDisplayName(medalId: String): String = MedalInfo.fromId(medalId)?.title ?: medalId

    fun resetState() {
        _uiState.value = CameraUiState.Idle
    }
}