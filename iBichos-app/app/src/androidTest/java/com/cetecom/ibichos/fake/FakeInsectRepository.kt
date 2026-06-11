package com.cetecom.ibichos.fake

import com.cetecom.ibichos.domain.model.InsectIdentification
import com.cetecom.ibichos.domain.repository.InsectRepository

/**
 * Implementación falsa de InsectRepository para tests de UI.
 *
 * Para simular un fallo de red al identificar el insecto:
 *   FakeInsectRepository.shouldFail = true
 *   FakeInsectRepository.errorMessage = "Error de red al analizar el insecto"
 * Resetear a false en @After para no contaminar otros tests.
 */
class FakeInsectRepository : InsectRepository {

    companion object {
        var shouldFail = false
        var errorMessage = "Error de red al analizar el insecto"
    }

    override suspend fun identify(
        imageBytes: ByteArray,
        lat: Double?,
        lon: Double?
    ): InsectIdentification {
        if (shouldFail) throw Exception(errorMessage)
        return InsectIdentification(
            scientificName = "Insectus testus",
            displayName    = "Insecto de prueba",
            probability    = 0.9,
            description    = ""
        )
    }
}
