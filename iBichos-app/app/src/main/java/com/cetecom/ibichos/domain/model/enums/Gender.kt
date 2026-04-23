package com.cetecom.ibichos.domain.model.enums

/**
 * Estandariza el género del usuario.
 * En la UI se mostrará en español; en Firestore se guarda el nombre del enum (ej: "MALE").
 */
enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY,
    UNSPECIFIED;

    /** Texto a mostrar en la app (en español). */
    fun displayName(): String = when (this) {
        MALE             -> "Masculino"
        FEMALE           -> "Femenino"
        OTHER            -> "Otro"
        PREFER_NOT_TO_SAY -> "Prefiero no decirlo"
        UNSPECIFIED      -> "No especificado"
    }
}
