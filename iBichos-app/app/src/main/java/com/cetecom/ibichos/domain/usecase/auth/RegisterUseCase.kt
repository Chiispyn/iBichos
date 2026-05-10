package com.cetecom.ibichos.domain.usecase.auth

import com.cetecom.ibichos.data.repository.EventRepositoryImpl
import com.cetecom.ibichos.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepositoryImpl
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ): String {
        val uid = authRepository.register(email, password, displayName, region, city, birthDate, gender)
        runCatching { eventRepository.logUserRegistered(uid) }
        return uid
    }
}
