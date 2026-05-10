package com.cetecom.ibichos.domain.usecase.capture

import com.cetecom.ibichos.domain.repository.CaptureRepository
import javax.inject.Inject

class DeleteCaptureUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(captureId: String) {
        captureRepository.deleteCapture(captureId)
    }
}
