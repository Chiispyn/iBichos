package com.cetecom.ibichos.domain.model

import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory

object InsectClassifier {

    fun inferCategory(scientificName: String): InsectCategory {
        val lower = scientificName.lowercase()
        return when {
            lower.contains("araneae")      || lower.contains("latrodectus") ||
            lower.contains("loxosceles")   || lower.contains("scorpion")    -> InsectCategory.ARACHNID
            lower.contains("coleoptera")   || lower.contains("coccinella")  ||
            lower.contains("carabus")      || lower.contains("dynastes")    -> InsectCategory.COLEOPTERA
            lower.contains("lepidoptera")  || lower.contains("papilio")     ||
            lower.contains("danaus")       || lower.contains("morpho")      -> InsectCategory.LEPIDOPTERA
            lower.contains("hymenoptera")  || lower.contains("apis")        ||
            lower.contains("bombus")       || lower.contains("vespula")     ||
            lower.contains("formica")                                        -> InsectCategory.HYMENOPTERA
            else -> InsectCategory.OTHER
        }
    }

    fun inferDangerLevel(category: InsectCategory): DangerLevel = when (category) {
        InsectCategory.ARACHNID    -> DangerLevel.VENOMOUS
        InsectCategory.LEPIDOPTERA -> DangerLevel.HARMLESS
        InsectCategory.COLEOPTERA  -> DangerLevel.HARMLESS
        InsectCategory.HYMENOPTERA -> DangerLevel.CAUTION
        else                       -> DangerLevel.UNKNOWN
    }
}
