package com.cetecom.ibichos.domain.model

data class InsectIdentification(
    val scientificName: String,
    val displayName: String,
    val probability: Double,
    val description: String
)
