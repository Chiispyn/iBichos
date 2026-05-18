package com.cetecom.ibichos.domain.usecase.auth

import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.repository.EventRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository
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
