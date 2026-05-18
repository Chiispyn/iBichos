package com.cetecom.ibichos.domain.usecase.capture

import com.cetecom.ibichos.domain.repository.EventRepository
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.GamificationConfig
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.model.enums.MedalInfo
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.cetecom.ibichos.domain.repository.UserRepository
import javax.inject.Inject

sealed class CaptureResult {
    data class Success(val message: String) : CaptureResult()
    data class LowConfidence(val probability: Double) : CaptureResult()
}

/**
 * Orquesta la persistencia de una captura y toda la lógica de gamificación.
 * El ViewModel es responsable de la subida a Cloudinary y la llamada a Kindwise;
 * este UseCase recibe los datos ya procesados y aplica las reglas de negocio.
 */
class ProcessCaptureUseCase @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(
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
    ): CaptureResult {
        // Reglas de moderación automática
        if (probability < 0.40) {
            return CaptureResult.LowConfidence(probability)
        }

        val needsReview = probability < 0.75
        val status = if (needsReview) "PENDING_REVIEW" else "APPROVED"

        val isNewInsect = !captureRepository.hasCaughtInsect(uid, scientificName)
        val xpGain = if (isNewInsect) GamificationConfig.XP_NEW_SPECIES else GamificationConfig.XP_REPEATED_SPECIES

        captureRepository.saveCapture(
            userId       = uid,
            imageUrl     = imageUrl,
            insectName   = insectName,
            scientificName = scientificName,
            category     = category,
            dangerLevel  = dangerLevel,
            probability  = probability,
            latitude     = lat,
            longitude    = lon,
            xpAwarded    = xpGain,
            description  = description,
            needsReview  = needsReview,
            status       = status
        )

        userRepository.incrementXp(uid, xpGain)

        val currentUser = userRepository.getUserProfile(uid)
        val medalsToUnlock = GamificationConfig.evaluateMedalsToUnlock(
            currentUser  = currentUser,
            isNewInsect  = isNewInsect,
            dangerLevel  = dangerLevel,
            category     = category
        )

        if (isNewInsect) {
            runCatching {
                eventRepository.logSpeciesDiscovered(
                    userId         = uid,
                    scientificName = scientificName,
                    insectName     = insectName,
                    category       = category.name,
                    xpAtEvent      = currentUser.gamification.xp + xpGain
                )
            }
        }

        userRepository.unlockMedalsAndIncrementUnique(
            uid              = uid,
            medalsToUnlock   = medalsToUnlock,
            isNewInsect      = isNewInsect,
            incrementCategory = category.name
        )

        val noveltyMsg = if (isNewInsect) "✨ ¡NUEVA ESPECIE DESCUBIERTA! ✨\n" else ""
        val reviewMsg  = if (needsReview) "\n⚠️ Pendiente de verificación" else ""
        val medalsMsg  = if (medalsToUnlock.isNotEmpty()) {
            "\n🏅 Logros: ${medalsToUnlock.joinToString { MedalInfo.fromId(it)?.title ?: it }}"
        } else ""

        return CaptureResult.Success(
            "${noveltyMsg}🦟 ¡Insecto Atrapado!\n$insectName\nConfianza: ${(probability * 100).toInt()}%\n🎁 +$xpGain XP$medalsMsg$reviewMsg"
        )
    }
}
