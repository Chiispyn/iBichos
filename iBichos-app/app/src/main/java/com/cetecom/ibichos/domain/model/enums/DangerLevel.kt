package com.cetecom.ibichos.domain.model.enums

/**
 * Nivel de peligro del insecto identificado.
 * La IA de Kindwise puede devolver esta clasificación; también puede asignarse manualmente
 * desde el Dashboard por un moderador.
 */
enum class DangerLevel {
    HARMLESS,
    CAUTION,
    VENOMOUS,
    UNKNOWN;

    /** Texto a mostrar en la app (en español) con emoji de color. */
    fun displayName(): String = when (this) {
        HARMLESS -> "Inofensivo 🟢"
        CAUTION  -> "Precaución 🟡"
        VENOMOUS -> "Venenoso 🔴"
        UNKNOWN  -> "Desconocido ⚪"
    }
}
