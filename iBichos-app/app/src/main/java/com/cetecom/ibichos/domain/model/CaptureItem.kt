package com.cetecom.ibichos.domain.model

import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory

/**
 * Modelo de dominio para una captura de insecto.
 *
 * Colección Firestore: captures/{captureId}
 * Esta capa NO depende de Firebase ni de ningún framework externo.
 *
 * Sistema de moderación (Human in the Loop):
 *   - La IA de Kindwise devuelve [probability] entre 0.0 y 1.0.
 *   - Si probability < 0.40 → la captura se rechaza automáticamente, se suma un strike al usuario.
 *   - Si 0.40 <= probability < 0.75 → la captura se acepta PERO [needsReview] = true.
 *     Esto permite que un moderador en el Dashboard revise la foto y corrija si es necesario.
 *   - Si probability >= 0.75 → captura automáticamente aprobada ([status] = "APPROVED").
 */
data class CaptureItem(
    val id: String = "",

    /** UID del usuario que realizó la captura. Relaciona esta captura con Users/{uid}. */
    val userId: String = "",

    /** URL de la imagen subida a Cloudinary. */
    val imageUrl: String = "",

    val insectName: String = "",
    val scientificName: String = "",

    /**
     * Categoría taxonómica del insecto. CLAVE para el sistema de logros.
     * La app consulta [categoryCounts] en GamificationData para desbloquear medallas:
     *   - 10 ARACHNID     → logro "Aracnólogo"
     *   - 10 LEPIDOPTERA  → logro "Lepidopterólogo"
     *   - etc.
     */
    val category: InsectCategory = InsectCategory.UNKNOWN,

    /** Probabilidad de acierto de la IA (0.0 a 1.0). */
    val probability: Double = 0.0,

    val dangerLevel: DangerLevel = DangerLevel.UNKNOWN,
    val latitude: Double? = null,
    val longitude: Double? = null,

    /** Milisegundos epoch — convertido desde Firebase Timestamp en el repositorio. */
    val capturedAt: Long = 0L,

    /** XP otorgada al usuario por esta captura. 0 si fue rechazada o el usuario está shadowbanned. */
    val xpAwarded: Long = 50L,

    val description: String = "",

    // --- Sistema de Moderación (Dashboard) ---
    /**
     * True si 0.40 <= probability < 0.75.
     * La captura se aceptó pero queda pendiente de revisión humana en el Dashboard.
     */
    val needsReview: Boolean = false,

    /**
     * Estado de la captura. Posibles valores:
     *   - "APPROVED"       → Aceptada automáticamente (probability >= 0.75)
     *   - "PENDING_REVIEW" → Pendiente de revisión humana (0.40 <= probability < 0.75)
     *   - "REJECTED"       → Rechazada (probability < 0.40 o moderador la eliminó)
     */
    val status: String = "APPROVED"
)
