package com.cetecom.ibichos.presentation.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.BuildConfig
import com.cetecom.ibichos.data.api.KindwiseApi
import com.cetecom.ibichos.data.api.KindwiseRequest
import com.cetecom.ibichos.data.repository.CaptureRepositoryImpl
import com.cetecom.ibichos.data.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed interface CameraUiState {
    object Idle    : CameraUiState
    object Loading : CameraUiState
    data class Success(val message: String) : CameraUiState
    data class Error(val message: String)   : CameraUiState
}

class CameraViewModel : ViewModel() {

    private val auth               = FirebaseAuth.getInstance()
    private val captureRepository  = CaptureRepositoryImpl()
    private val userRepository     = UserRepositoryImpl()
    private val api                = KindwiseApi.create()

    // ── MODO EMULADOR ─────────────────────────────────────────────────────────
    // Cambiar a false cuando se use en un dispositivo físico con API key real
    private val isEmulatorMode = true
    // ─────────────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /**
     * Punto de entrada: recibe el bitmap capturado por CameraX y la ubicación GPS.
     * El ViewModel maneja todo el procesamiento en background (IO → repositorios).
     */
    fun processCapture(bitmap: Bitmap, lat: Double?, lon: Double?, context: Context) {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = CameraUiState.Error("No hay usuario autenticado")
            return
        }

        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading

            try {
                // 1. Guardar imagen localmente (como URL local hasta implementar Storage)
                val filename = UUID.randomUUID().toString() + ".jpg"
                val file = File(context.filesDir, filename)
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
                    fos.flush()
                }
                val localImageUrl = file.absolutePath

                if (isEmulatorMode) {
                    // Datos de prueba para el emulador
                    saveCaptureData(
                        uid           = uid,
                        imageUrl      = localImageUrl,
                        insectName    = "ABEJA",
                        scientificName = "Apis mellifera",
                        dangerLevel   = "Precaución: Aguijón",
                        probability   = 0.99,
                        lat           = lat,
                        lon           = lon,
                        description   = "La abeja europea (Apis mellifera) es una especie de himenóptero apócrito de la familia Apidae. Es la especie de abeja con mayor distribución en el mundo. Son insectos polinizadores cruciales para la supervivencia de ecosistemas y para la polinización cruzada de la agricultura mundial."
                    )
                } else {
                    // 2. Convertir a Base64 para la API
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                    // 3. Llamada a Kindwise (suspend — no bloquea el hilo principal)
                    val response = api.identifyInsect(
                        apiKey  = BuildConfig.KINDWISE_API_KEY,
                        request = KindwiseRequest(
                            images    = listOf("data:image/jpeg;base64,$base64"),
                            latitude  = lat,
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
                    val commonName     = suggestion.details?.common_names?.firstOrNull() ?: ""
                    val displayName    = if (commonName.isNotEmpty())
                        commonName.uppercase() else scientificName.uppercase()
                    val prob           = suggestion.probability ?: 0.0
                    val dangerLevels   = listOf("Inofensivo", "Precaución", "Venenoso", "Plaga")
                    val dangerLevel    = dangerLevels.random()
                    val description    = suggestion.details?.description?.value ?: "Insecto registrado taxonómicamente por la IA Kindwise. Se recomienda realizar un cruce de datos con enciclopedias especializadas para confirmar hábitos alimenticios y zona biológica nativa. Mantener respeto por el ecosistema local."

                    saveCaptureData(uid, localImageUrl, displayName, scientificName, dangerLevel, prob, lat, lon, description)
                }
            } catch (e: Exception) {
                _uiState.value = CameraUiState.Error("Error: ${e.message}")
            }
        }
    }

    private suspend fun saveCaptureData(
        uid: String, imageUrl: String, insectName: String, scientificName: String,
        dangerLevel: String, probability: Double, lat: Double?, lon: Double?, description: String
    ) {
        val isNewInsect = !captureRepository.hasCaughtInsect(uid, scientificName)
        val xpGain = if (isNewInsect) 150L else 20L
        
        captureRepository.saveCapture(
            userId        = uid,
            imageUrl      = imageUrl,
            insectName    = insectName,
            scientificName = scientificName,
            dangerLevel   = dangerLevel,
            probability   = probability,
            latitude      = lat,
            longitude     = lon,
            xpAwarded     = xpGain,
            description   = description
        )
        
        userRepository.incrementXp(uid, xpGain)

        // Evaluar Medallas y Logros con umbrales rigurosos
        val currentUser = userRepository.getUserProfile(uid)
        val currentMedals = currentUser.medals
        val newUniqueCount = currentUser.uniqueInsectsCount + if (isNewInsect) 1 else 0
        
        val medalsToUnlock = mutableListOf<String>()
        var incrementCategory: String? = null
        val nameLower = insectName.lowercase()

        // Determinar qué cuota subir
        if (nameLower.contains("araña")) incrementCategory = "aranas"
        else if (nameLower.contains("mariposa") || nameLower.contains("polilla")) incrementCategory = "lepidopteros"
        else if (nameLower.contains("abeja") || nameLower.contains("avispa")) incrementCategory = "polinizadores"
        else if (nameLower.contains("escarabajo")) incrementCategory = "coleopteros"
        else if (dangerLevel.contains("Plaga", ignoreCase = true)) incrementCategory = "plagas"

        // Calcular nuevo valor de la sub-cuenta
        val newCategoryCount = if (incrementCategory != null) {
            (currentUser.categoryCounts[incrementCategory] ?: 0) + 1
        } else 0

        if (isNewInsect && newUniqueCount == 1 && !currentMedals.contains("Primer Avistamiento")) {
            medalsToUnlock.add("Primer Avistamiento")
        }
        if (isNewInsect && newUniqueCount == 5 && !currentMedals.contains("Investigador Novato")) {
            medalsToUnlock.add("Investigador Novato")
        }
        if ((dangerLevel.contains("Venenoso", ignoreCase = true) || dangerLevel.contains("Peligroso", ignoreCase = true)) 
            && !currentMedals.contains("Cazador Valiente")) {
            medalsToUnlock.add("Cazador Valiente")
        }
        
        // Logros de Dificultad por especies repetitivas
        if (incrementCategory == "aranas" && newCategoryCount >= 5 && !currentMedals.contains("Aracnólogo")) {
            medalsToUnlock.add("Aracnólogo")
        }
        if (incrementCategory == "lepidopteros" && newCategoryCount >= 5 && !currentMedals.contains("Lepidopterólogo")) {
            medalsToUnlock.add("Lepidopterólogo")
        }
        if (incrementCategory == "polinizadores" && newCategoryCount >= 10 && !currentMedals.contains("Amigo de Polinizadores")) {
            medalsToUnlock.add("Amigo de Polinizadores")
        }
        if (incrementCategory == "coleopteros" && newCategoryCount >= 15 && !currentMedals.contains("Coleopterólogo")) {
            medalsToUnlock.add("Coleopterólogo")
        }
        if (incrementCategory == "plagas" && newCategoryCount >= 10 && !currentMedals.contains("Control de Plagas")) {
            medalsToUnlock.add("Control de Plagas")
        }

        userRepository.unlockMedalsAndIncrementUnique(uid, medalsToUnlock, isNewInsect, incrementCategory)

        val noveltyMsg = if (isNewInsect) "✨ ¡NUEVA ESPECIE DESCUBIERTA! ✨\n" else ""
        val medalsMsg = if (medalsToUnlock.isNotEmpty()) "\n🏅 Logros: ${medalsToUnlock.joinToString()}" else ""

        _uiState.value = CameraUiState.Success(
            "${noveltyMsg}🦟 ¡Insecto Atrapado!\n$insectName\nConfianza: ${(probability * 100).toInt()}%\n🎁 +$xpGain XP$medalsMsg"
        )
    }

    fun resetState() { _uiState.value = CameraUiState.Idle }
}

