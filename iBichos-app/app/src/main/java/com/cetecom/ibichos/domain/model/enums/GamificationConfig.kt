package com.cetecom.ibichos.domain.model.enums

import com.cetecom.ibichos.domain.model.UserProfile

/**
 * Archivo centralizado de reglas de gamificación.
 * Aquí se configuran los puntos otorgados, los umbrales de experiencia para subir de nivel,
 * y las reglas de negocio para desbloquear medallas.
 */
object GamificationConfig {

    // ── 1. PUNTOS DE EXPERIENCIA (XP) ─────────────────────────────────────────
    
    /** Puntos otorgados por capturar una especie por primera vez. */
    const val XP_NEW_SPECIES = 150L
    
    /** Puntos otorgados por capturar una especie que ya estaba registrada. */
    const val XP_REPEATED_SPECIES = 20L

    // ── 2. UMBRALES DE NIVELES ──────────────────────────────────────────────
    
    const val THRESHOLD_AMATEUR = 100L
    const val THRESHOLD_EXPLORER = 500L
    const val THRESHOLD_ENTOMOLOGIST = 1500L
    const val THRESHOLD_BUG_MASTER = 5000L

    // ── 3. REQUISITOS DE MEDALLAS ───────────────────────────────────────────
    
    const val REQ_NOVICE_RESEARCHER = 5     // Especies únicas necesarias
    const val REQ_ARACHNOLOGIST = 10        // Arácnidos necesarios
    const val REQ_LEPIDOPTEROLOGIST = 10    // Mariposas necesarias
    const val REQ_POLLINATOR_FRIEND = 10    // Abejas/Avispas necesarias
    const val REQ_COLEOPTEROLOGIST = 15     // Escarabajos necesarios

    /**
     * Calcula el nivel correspondiente a la XP acumulada del usuario.
     */
    fun calculateLevel(xp: Long): UserLevel = when {
        xp < THRESHOLD_AMATEUR      -> UserLevel.CASUAL
        xp < THRESHOLD_EXPLORER     -> UserLevel.AMATEUR
        xp < THRESHOLD_ENTOMOLOGIST -> UserLevel.EXPLORER
        xp < THRESHOLD_BUG_MASTER   -> UserLevel.ENTOMOLOGIST
        else                        -> UserLevel.BUG_MASTER
    }

    /** Calcula cuánta XP se necesita para el siguiente nivel, o devuelve el máximo si ya está en el último. */
    fun getNextLevelXp(currentXp: Long): Long = when {
        currentXp < THRESHOLD_AMATEUR      -> THRESHOLD_AMATEUR
        currentXp < THRESHOLD_EXPLORER     -> THRESHOLD_EXPLORER
        currentXp < THRESHOLD_ENTOMOLOGIST -> THRESHOLD_ENTOMOLOGIST
        currentXp < THRESHOLD_BUG_MASTER   -> THRESHOLD_BUG_MASTER
        else                               -> THRESHOLD_BUG_MASTER
    }

    // ── 3. REGLAS DE MEDALLAS ───────────────────────────────────────────────
    
    /**
     * Evalúa qué medallas deben desbloquearse tras una captura exitosa.
     * @param currentUser El perfil actual del usuario antes de procesar esta captura.
     * @param isNewInsect Booleano indicando si es el primer descubrimiento de esta especie.
     * @param dangerLevel El nivel de peligro del insecto capturado.
     * @param category La categoría taxonómica del insecto.
     * @return Una lista de IDs de medallas que se acaban de desbloquear.
     */
    fun evaluateMedalsToUnlock(
        currentUser: UserProfile,
        isNewInsect: Boolean,
        dangerLevel: DangerLevel,
        category: InsectCategory
    ): List<String> {
        val currentMedals = currentUser.gamification.medals
        val newUniqueCount = currentUser.gamification.uniqueInsectsCount + if (isNewInsect) 1 else 0
        val categoryName = category.name
        val newCategoryCount = (currentUser.gamification.categoryCounts[categoryName] ?: 0) + 1

        val unlockedMedals = mutableListOf<String>()

        // 🥉 Progreso General
        if (isNewInsect && newUniqueCount >= 1 && !currentMedals.contains(MedalInfo.FIRST_CAPTURE.id)) {
            unlockedMedals.add(MedalInfo.FIRST_CAPTURE.id)
        }
        if (isNewInsect && newUniqueCount >= REQ_NOVICE_RESEARCHER && !currentMedals.contains(MedalInfo.NOVICE_RESEARCHER.id)) {
            unlockedMedals.add(MedalInfo.NOVICE_RESEARCHER.id)
        }

        // 💀 Peligro
        if (dangerLevel == DangerLevel.VENOMOUS && !currentMedals.contains(MedalInfo.BRAVE_HUNTER.id)) {
            unlockedMedals.add(MedalInfo.BRAVE_HUNTER.id)
        }

        // 🐛 Taxonomía
        if (category == InsectCategory.ARACHNID && newCategoryCount >= REQ_ARACHNOLOGIST && !currentMedals.contains(MedalInfo.ARACHNOLOGIST.id)) {
            unlockedMedals.add(MedalInfo.ARACHNOLOGIST.id)
        }
        if (category == InsectCategory.LEPIDOPTERA && newCategoryCount >= REQ_LEPIDOPTEROLOGIST && !currentMedals.contains(MedalInfo.LEPIDOPTEROLOGIST.id)) {
            unlockedMedals.add(MedalInfo.LEPIDOPTEROLOGIST.id)
        }
        if (category == InsectCategory.HYMENOPTERA && newCategoryCount >= REQ_POLLINATOR_FRIEND && !currentMedals.contains(MedalInfo.POLLINATOR_FRIEND.id)) {
            unlockedMedals.add(MedalInfo.POLLINATOR_FRIEND.id)
        }
        if (category == InsectCategory.COLEOPTERA && newCategoryCount >= REQ_COLEOPTEROLOGIST && !currentMedals.contains(MedalInfo.COLEOPTEROLOGIST.id)) {
            unlockedMedals.add(MedalInfo.COLEOPTEROLOGIST.id)
        }

        return unlockedMedals
    }
}
