package com.cetecom.ibichos.domain.usecase.capture

import com.cetecom.ibichos.domain.model.InsectIdentification
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.repository.ImageRepository
import com.cetecom.ibichos.domain.repository.InsectRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AnalyzeCaptureUseCaseTest {

    // ── Mocks ────────────────────────────────────────────────────────────────
    private val imageRepository: ImageRepository         = mockk()
    private val insectRepository: InsectRepository       = mockk()
    private val processCaptureUseCase: ProcessCaptureUseCase = mockk()

    private lateinit var useCase: AnalyzeCaptureUseCase

    // ── Datos base ───────────────────────────────────────────────────────────
    private val uid        = "user123"
    private val imageBytes = byteArrayOf(1, 2, 3)
    private val lat        = 40.4168
    private val lon        = -3.7038
    private val imageUrl   = "https://cdn.example.com/bug.jpg"

    private val identification = InsectIdentification(
        displayName    = "Araña Lobo",
        scientificName = "Lycosa tarantula",
        probability    = 0.85,
        description    = "Araña de gran tamaño."
    )

    @Before
    fun setUp() {
        useCase = AnalyzeCaptureUseCase(imageRepository, insectRepository, processCaptureUseCase)

        coEvery { imageRepository.upload(imageBytes) }                         returns imageUrl
        coEvery { insectRepository.identify(imageBytes, lat, lon) }            returns identification
        coEvery { processCaptureUseCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns
                CaptureResult.Success("¡Insecto Atrapado!")
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 1 — Flujo feliz
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `flujo feliz sube imagen identifica el insecto y llama a processCaptureUseCase con datos correctos`() = runTest {
        val result = useCase(uid, imageBytes, lat, lon)

        // Verifica que se subió la imagen
        coVerify(exactly = 1) { imageRepository.upload(imageBytes) }

        // Verifica que se llamó a la IA con los bytes y coordenadas
        coVerify(exactly = 1) { insectRepository.identify(imageBytes, lat, lon) }

        // Verifica que se procesó con los datos correctos
        coVerify(exactly = 1) {
            processCaptureUseCase(
                uid            = uid,
                imageUrl       = imageUrl,
                insectName     = identification.displayName,
                scientificName = identification.scientificName,
                probability    = identification.probability,
                lat            = lat,
                lon            = lon,
                description    = identification.description,
                category       = any(),
                dangerLevel    = any()
            )
        }

        assertTrue(result is CaptureResult.Success)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 2 — Falla en la subida de imagen
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `si imageRepository upload lanza excepcion se propaga y no llama a la IA`() = runTest {
        coEvery { imageRepository.upload(imageBytes) } throws RuntimeException("Cloudinary caído")

        var thrownException: Exception? = null
        try {
            useCase(uid, imageBytes, lat, lon)
        } catch (e: Exception) {
            thrownException = e
        }

        assertNotNull("Debe propagar la excepción", thrownException)
        assertEquals("Cloudinary caído", thrownException!!.message)

        // La IA no debe ser llamada si la subida falla
        coVerify(exactly = 0) { insectRepository.identify(any(), any(), any()) }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 3 — Falla en la identificación de la IA
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `si insectRepository identify lanza excepcion se propaga y no llama a processCaptureUseCase`() = runTest {
        coEvery { insectRepository.identify(imageBytes, lat, lon) } throws RuntimeException("Kindwise caído")

        var thrownException: Exception? = null
        try {
            useCase(uid, imageBytes, lat, lon)
        } catch (e: Exception) {
            thrownException = e
        }

        assertNotNull("Debe propagar la excepción", thrownException)
        assertEquals("Kindwise caído", thrownException!!.message)

        // ProcessCapture no debe ser llamado si la IA falla
        coVerify(exactly = 0) { processCaptureUseCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 4 — GPS nulo
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `lat y lon nulas se pasan correctamente sin romper el flujo`() = runTest {
        coEvery { insectRepository.identify(imageBytes, null, null) } returns identification

        val result = useCase(uid, imageBytes, null, null)

        coVerify(exactly = 1) { insectRepository.identify(imageBytes, null, null) }

        coVerify(exactly = 1) {
            processCaptureUseCase(
                uid            = uid,
                imageUrl       = imageUrl,
                insectName     = any(),
                scientificName = any(),
                probability    = any(),
                lat            = null,
                lon            = null,
                description    = any(),
                category       = any(),
                dangerLevel    = any()
            )
        }

        assertTrue(result is CaptureResult.Success)
    }
}
