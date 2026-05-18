package com.cetecom.ibichos.domain.usecase.auth

import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.repository.EventRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository
) {
    /** @return uid del usuario autenticado */
    suspend operator fun invoke(idToken: String): String {
        val (uid, isNewUser) = authRepository.signInWithGoogle(idToken)
        if (isNewUser) runCatching { eventRepository.logUserRegistered(uid) }
        return uid
    }
}
