package com.cetecom.ibichos.data.repository

import android.util.Base64
import com.cetecom.ibichos.BuildConfig
import com.cetecom.ibichos.data.remote.api.KindwiseApi
import com.cetecom.ibichos.data.remote.dto.KindwiseRequest
import com.cetecom.ibichos.domain.model.InsectIdentification
import com.cetecom.ibichos.domain.repository.InsectRepository
import javax.inject.Inject

class InsectRepositoryImpl @Inject constructor(
    private val kindwiseApi: KindwiseApi
) : InsectRepository {

    override suspend fun identify(imageBytes: ByteArray, lat: Double?, lon: Double?): InsectIdentification {
        val base64   = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val response = kindwiseApi.identifyInsect(
            apiKey  = BuildConfig.KINDWISE_API_KEY,
            request = KindwiseRequest(listOf("data:image/jpeg;base64,$base64"), lat, lon)
        )

        val suggestion = response.result?.classification?.suggestions?.firstOrNull()
            ?: throw Exception("No se identificó ningún insecto. Intenta con otra imagen.")

        val scientificName = suggestion.name ?: "Desconocido"
        val commonName     = suggestion.details?.common_names?.firstOrNull() ?: ""
        val displayName    = if (commonName.isNotEmpty()) commonName.uppercase() else scientificName.uppercase()

        return InsectIdentification(
            scientificName = scientificName,
            displayName    = displayName,
            probability    = suggestion.probability ?: 0.0,
            description    = suggestion.details?.description?.value
                ?: "Insecto registrado taxonómicamente por la IA Kindwise."
        )
    }
}
