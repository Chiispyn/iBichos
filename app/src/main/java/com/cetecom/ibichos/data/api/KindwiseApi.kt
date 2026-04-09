package com.cetecom.ibichos.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Interfaz Retrofit para la API de Kindwise.
 * Usa suspend fun (coroutines nativas desde Retrofit 2.6).
 */
interface KindwiseApi {

    @POST("v1/identification?details=common_names,url&language=es")
    suspend fun identifyInsect(
        @Header("Api-Key") apiKey: String,
        @Body request: KindwiseRequest
    ): KindwiseResponse

    companion object {
        private const val BASE_URL = "https://insect.kindwise.com/api/"

        fun create(): KindwiseApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(KindwiseApi::class.java)
        }
    }
}

// ── DTOs ─────────────────────────────────────────────────────────────────────

data class KindwiseRequest(
    val images: List<String>,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class KindwiseResponse(
    val result: KindwiseResult?
)

data class KindwiseResult(
    val classification: Classification?
)

data class Classification(
    val suggestions: List<Suggestion>?
)

data class Suggestion(
    val name: String?,
    val probability: Double?,
    val details: Details?
)

data class Details(
    val common_names: List<String>?,
    val url: String?
)
