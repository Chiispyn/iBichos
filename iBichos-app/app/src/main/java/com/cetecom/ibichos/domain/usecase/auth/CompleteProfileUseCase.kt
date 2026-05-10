package com.cetecom.ibichos.domain.usecase.auth

import com.cetecom.ibichos.domain.repository.AuthRepository
import javax.inject.Inject

class CompleteProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        uid: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ) {
        authRepository.completeProfile(uid, region, city, birthDate, gender)
    }
}
