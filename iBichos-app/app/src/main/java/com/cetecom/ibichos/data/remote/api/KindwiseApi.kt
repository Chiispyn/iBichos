package com.cetecom.ibichos.data.remote.api

import com.cetecom.ibichos.data.remote.dto.KindwiseRequest
import com.cetecom.ibichos.data.remote.dto.KindwiseResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface KindwiseApi {

    @POST("v1/identification?details=common_names,url,description&language=es")
    suspend fun identifyInsect(
        @Header("Api-Key") apiKey: String,
        @Body request: KindwiseRequest
    ): KindwiseResponse

    companion object {
        private const val BASE_URL = "https://insect.kindwise.com/api/"

        fun create(): KindwiseApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KindwiseApi::class.java)
    }
}
