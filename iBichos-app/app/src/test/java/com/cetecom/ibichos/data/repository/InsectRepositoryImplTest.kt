package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.data.remote.api.KindwiseApi
import com.cetecom.ibichos.data.remote.dto.Classification
import com.cetecom.ibichos.data.remote.dto.DescriptionWrapper
import com.cetecom.ibichos.data.remote.dto.Details
import com.cetecom.ibichos.data.remote.dto.KindwiseRequest
import com.cetecom.ibichos.data.remote.dto.KindwiseResponse
import com.cetecom.ibichos.data.remote.dto.KindwiseResult
import com.cetecom.ibichos.data.remote.dto.Suggestion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InsectRepositoryImplTest {

    // ── Mocks ────────────────────────────────────────────────────────────────
    private val kindwiseApi: KindwiseApi = mockk()

    private lateinit var repository: InsectRepositoryImpl

    private val imageBytes = ByteArray(10) { it.toByte() }
    private val lat = 40.4168
    private val lon = -3.7038

    @Before
    fun setUp() {
        repository = InsectRepositoryImpl(kindwiseApi)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 1 — Identificación exitosa con nombre común
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `identify retorna displayName en mayusculas usando el nombre comun`() = runTest {
        coEvery { kindwiseApi.identifyInsect(any(), any()) } returns buildResponse(
            scientificName = "Lycosa tarantula",
            commonNames    = listOf("Wolf Spider"),
            probability    = 0.90,
            description    = "Araña terrestre de gran tamaño."
        )

        val result = repository.identify(imageBytes, lat, lon)

        assertEquals("WOLF SPIDER", result.displayName)
        assertEquals("Lycosa tarantula", result.scientificName)
        assertEquals(0.90, result.probability, 0.001)
        assertEquals("Araña terrestre de gran tamaño.", result.description)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 2 — Sin nombre común, usa nombre científico
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `identify usa scientificName en mayusculas cuando no hay nombre comun`() = runTest {
        coEvery { kindwiseApi.identifyInsect(any(), any()) } returns buildResponse(
            scientificName = "Apis mellifera",
            commonNames    = emptyList(),
            probability    = 0.85
        )

        val result = repository.identify(imageBytes, lat, lon)

        assertEquals("APIS MELLIFERA", result.displayName)
        assertEquals("Apis mellifera", result.scientificName)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 3 — Sin descripción, usa texto por defecto
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `identify usa descripcion por defecto cuando la API no retorna descripcion`() = runTest {
        coEvery { kindwiseApi.identifyInsect(any(), any()) } returns buildResponse(
            scientificName = "Apis mellifera",
            commonNames    = listOf("Honey Bee"),
            probability    = 0.80,
            description    = null
        )

        val result = repository.identify(imageBytes, lat, lon)

        assertEquals("Insecto registrado taxonómicamente por la IA Kindwise.", result.description)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 4 — Sin suggestions, lanza excepción
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `identify lanza excepcion cuando no hay suggestions`() = runTest {
        coEvery { kindwiseApi.identifyInsect(any(), any()) } returns KindwiseResponse(
            result = KindwiseResult(
                classification = Classification(suggestions = emptyList())
            )
        )

        val exception = runCatching { repository.identify(imageBytes, lat, lon) }.exceptionOrNull()

        assertNotNull(exception)
        assertEquals("No se identificó ningún insecto. Intenta con otra imagen.", exception?.message)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 5 — Respuesta completamente nula, lanza excepción
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `identify lanza excepcion cuando result es null`() = runTest {
        coEvery { kindwiseApi.identifyInsect(any(), any()) } returns KindwiseResponse(result = null)

        val exception = runCatching { repository.identify(imageBytes, lat, lon) }.exceptionOrNull()

        assertNotNull(exception)
        assertEquals("No se identificó ningún insecto. Intenta con otra imagen.", exception?.message)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 6 — Probability nula en suggestion, usa 0.0
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `identify retorna probability 0_0 cuando la sugerencia no tiene probabilidad`() = runTest {
        coEvery { kindwiseApi.identifyInsect(any(), any()) } returns buildResponse(
            scientificName = "Apis mellifera",
            commonNames    = listOf("Honey Bee"),
            probability    = null
        )

        val result = repository.identify(imageBytes, lat, lon)

        assertEquals(0.0, result.probability, 0.001)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 7 — Coordenadas nulas se pasan a la API
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `identify pasa lat y lon nulas a la API sin lanzar excepcion`() = runTest {
        coEvery { kindwiseApi.identifyInsect(any(), any()) } returns buildResponse(
            scientificName = "Apis mellifera",
            commonNames    = listOf("Honey Bee"),
            probability    = 0.80
        )

        val result = repository.identify(imageBytes, null, null)

        assertNotNull(result)
        coVerify {
            kindwiseApi.identifyInsect(
                apiKey  = any(),
                request = match { it.latitude == null && it.longitude == null }
            )
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 8 — La API lanza excepción de red
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `identify propaga la excepcion cuando la API falla`() = runTest {
        coEvery { kindwiseApi.identifyInsect(any(), any()) } throws RuntimeException("Sin conexión")

        val exception = runCatching { repository.identify(imageBytes, lat, lon) }.exceptionOrNull()

        assertNotNull(exception)
        assertEquals("Sin conexión", exception?.message)
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helper
    // ════════════════════════════════════════════════════════════════════════

    private fun buildResponse(
        scientificName: String,
        commonNames: List<String>,
        probability: Double?,
        description: String? = "Descripción de prueba."
    ): KindwiseResponse = KindwiseResponse(
        result = KindwiseResult(
            classification = Classification(
                suggestions = listOf(
                    Suggestion(
                        name        = scientificName,
                        probability = probability,
                        details     = Details(
                            common_names = commonNames,
                            url          = null,
                            description  = description?.let { DescriptionWrapper(it) }
                        )
                    )
                )
            )
        )
    )
}
