package com.cetecom.ibichos.domain.usecase.capture

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.repository.CaptureRepository
import javax.inject.Inject

class GetCapturesUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(userId: String): List<CaptureItem> =
        captureRepository.getCaptures(userId)
}
