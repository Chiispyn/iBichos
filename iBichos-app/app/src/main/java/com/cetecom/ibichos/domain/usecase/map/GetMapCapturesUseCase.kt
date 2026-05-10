package com.cetecom.ibichos.domain.usecase.map

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.repository.CaptureRepository
import javax.inject.Inject

class GetMapCapturesUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(userId: String, isGlobal: Boolean): List<CaptureItem> =
        if (isGlobal) captureRepository.getGlobalCaptures(200)
        else captureRepository.getCaptures(userId)
}
