package com.cetecom.ibichos.domain.usecase.auth

import com.cetecom.ibichos.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        authRepository.login(email, password)
    }
}
