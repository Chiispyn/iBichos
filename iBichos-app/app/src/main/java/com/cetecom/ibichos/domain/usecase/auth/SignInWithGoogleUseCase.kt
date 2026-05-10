package com.cetecom.ibichos.domain.usecase.auth

import com.cetecom.ibichos.data.repository.EventRepositoryImpl
import com.cetecom.ibichos.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepositoryImpl
) {
    /** @return uid del usuario autenticado */
    suspend operator fun invoke(idToken: String): String {
        val (uid, isNewUser) = authRepository.signInWithGoogle(idToken)
        if (isNewUser) runCatching { eventRepository.logUserRegistered(uid) }
        return uid
    }
}
