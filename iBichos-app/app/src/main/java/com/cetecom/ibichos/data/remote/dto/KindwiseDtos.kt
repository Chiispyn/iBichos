package com.cetecom.ibichos.data.remote.dto

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
    val url: String?,
    val description: DescriptionWrapper?
)

data class DescriptionWrapper(
    val value: String?
)
