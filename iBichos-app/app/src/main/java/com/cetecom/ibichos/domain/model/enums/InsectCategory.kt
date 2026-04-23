package com.cetecom.ibichos.domain.model.enums

/**
 * Categoría taxonómica del insecto capturado.
 * Este campo es CLAVE para el sistema de logros: la app consulta cuántas capturas
 * tiene el usuario por categoría y desbloquea medallas como "Aracnólogo" o "Lepidopterólogo".
 *
 * Mapeo desde Kindwise (orden taxonómico → InsectCategory):
 *   - Araneae, Scorpiones, etc. → ARACHNID
 *   - Coleoptera               → COLEOPTERA
 *   - Lepidoptera              → LEPIDOPTERA
 *   - Hymenoptera              → HYMENOPTERA
 *   - Cualquier otro orden     → OTHER
 *   - Sin datos / error        → UNKNOWN
 */
enum class InsectCategory {
    ARACHNID,
    COLEOPTERA,
    LEPIDOPTERA,
    HYMENOPTERA,
    OTHER,
    UNKNOWN;

    /** Texto a mostrar en la app (en español). */
    fun displayName(): String = when (this) {
        ARACHNID    -> "Arácnido"
        COLEOPTERA  -> "Coleóptero"
        LEPIDOPTERA -> "Lepidóptero"
        HYMENOPTERA -> "Himenóptero"
        OTHER       -> "Otro"
        UNKNOWN     -> "Desconocido"
    }
}
