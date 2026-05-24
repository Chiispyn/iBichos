package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.data.remote.api.CloudinaryApi
import com.cetecom.ibichos.domain.repository.ImageRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val cloudinaryApi: CloudinaryApi
) : ImageRepository {

    private val cloudName    = "dmfmzlozw"
    private val uploadPreset = "IBichos"

    override suspend fun upload(imageBytes: ByteArray): String {
        val filePart   = MultipartBody.Part.createFormData(
            name     = "file",
            filename = "image.jpg",
            body     = imageBytes.toRequestBody("image/jpeg".toMediaType())
        )
        val presetBody = uploadPreset.toRequestBody("text/plain".toMediaType())
        return cloudinaryApi.uploadImage(cloudName, filePart, presetBody).secure_url
            ?: throw Exception("Cloudinary no devolvió una URL válida")
    }
}
