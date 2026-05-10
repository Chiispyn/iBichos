package com.cetecom.ibichos.domain.usecase.auth

import com.cetecom.ibichos.domain.repository.AuthRepository
import javax.inject.Inject

class GetLocationsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Map<String, List<String>> =
        authRepository.getLocations()
}
