package com.cetecom.ibichos.domain.usecase.capture

import com.cetecom.ibichos.domain.service.InsectClassifier
import com.cetecom.ibichos.domain.repository.ImageRepository
import com.cetecom.ibichos.domain.repository.InsectRepository
import javax.inject.Inject

class AnalyzeCaptureUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val insectRepository: InsectRepository,
    private val processCaptureUseCase: ProcessCaptureUseCase
) {
    suspend operator fun invoke(
        uid: String,
        imageBytes: ByteArray,
        lat: Double?,
        lon: Double?
    ): CaptureResult {
        val imageUrl       = imageRepository.upload(imageBytes)
        val identification = insectRepository.identify(imageBytes, lat, lon)
        val category       = InsectClassifier.inferCategory(identification.scientificName)
        val dangerLevel    = InsectClassifier.inferDangerLevel(category)

        return processCaptureUseCase(
            uid            = uid,
            imageUrl       = imageUrl,
            insectName     = identification.displayName,
            scientificName = identification.scientificName,
            category       = category,
            dangerLevel    = dangerLevel,
            probability    = identification.probability,
            lat            = lat,
            lon            = lon,
            description    = identification.description
        )
    }
}
