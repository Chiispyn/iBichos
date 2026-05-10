package com.cetecom.ibichos.domain.usecase.profile

import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.repository.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String): UserProfile =
        userRepository.getUserProfile(uid)
}
