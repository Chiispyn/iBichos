package com.cetecom.ibichos.domain.usecase.capture

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.repository.CaptureRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetCapturesUseCaseTest {

    private val captureRepository: CaptureRepository = mockk()
    private lateinit var useCase: GetCapturesUseCase

    private val userId = "user123"

    @Before
    fun setUp() {
        useCase = GetCapturesUseCase(captureRepository)
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 1 — Usuario con capturas
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `usuario con capturas devuelve la lista correcta`() = runTest {
        val capturas = listOf(
            mockk<CaptureItem>(),
            mockk<CaptureItem>()
        )
        coEvery { captureRepository.getCaptures(userId) } returns capturas

        val result = useCase(userId)

        assertEquals(2, result.size)
        assertEquals(capturas, result)
        coVerify(exactly = 1) { captureRepository.getCaptures(userId) }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CASO 2 — Usuario sin capturas
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `usuario sin capturas devuelve lista vacia`() = runTest {
        coEvery { captureRepository.getCaptures(userId) } returns emptyList()

        val result = useCase(userId)

        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { captureRepository.getCaptures(userId) }
    }
}
