package com.cetecom.ibichos.domain.usecase.profile

import com.cetecom.ibichos.domain.repository.ImageRepository
import com.cetecom.ibichos.domain.repository.UserRepository
import java.io.File
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String, imageFile: File): String {
        val url = imageRepository.upload(imageFile.readBytes())
        return userRepository.updateAvatar(uid, url)
    }
}
