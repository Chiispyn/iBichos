package com.cetecom.ibichos.domain.usecase.profile

import com.cetecom.ibichos.data.repository.CloudinaryApi
import com.cetecom.ibichos.domain.repository.UserRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val cloudinaryApi: CloudinaryApi,
    private val userRepository: UserRepository
) {
    private val cloudName    = "drubfka1z"
    private val uploadPreset = "IBichos"

    /** Sube [imageFile] a Cloudinary, guarda la URL en Firestore y la devuelve. */
    suspend operator fun invoke(uid: String, imageFile: File): String {
        val requestFile = imageFile.asRequestBody("image/*".toMediaType())
        val filePart    = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
        val presetBody  = uploadPreset.toRequestBody("text/plain".toMediaType())

        val response = cloudinaryApi.uploadImage(
            cloudName    = cloudName,
            file         = filePart,
            uploadPreset = presetBody
        )

        val url = response.secure_url ?: throw Exception("Cloudinary no devolvió una URL válida")
        return userRepository.updateAvatar(uid, url)
    }
}
