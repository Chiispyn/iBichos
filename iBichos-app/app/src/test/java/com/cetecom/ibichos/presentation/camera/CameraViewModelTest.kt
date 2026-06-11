package com.cetecom.ibichos.presentation.camera

import android.graphics.Bitmap
import com.cetecom.ibichos.dispatcher.MainDispatcherRule
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.capture.AnalyzeCaptureUseCase
import com.cetecom.ibichos.domain.usecase.capture.CaptureResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    // 1. Inyectamos a nuestro "doble de riesgo" para el hilo principal
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 2. Mocks de las dependencias
    private val authRepository: AuthRepository = mockk()
    private val analyzeCaptureUseCase: AnalyzeCaptureUseCase = mockk()

    private lateinit var viewModel: CameraViewModel

    @Before
    fun setup() {
        viewModel = CameraViewModel(authRepository, analyzeCaptureUseCase)
    }

    @Test
    fun `processCapture sin usuario logueado emite Error inmediatamente`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserId() } returns null
        val mockBitmap = mockk<Bitmap>(relaxed = true)

        // Act
        viewModel.processCapture(mockBitmap, null, null)

        // Assert
        val estado = viewModel.uiState.value
        assertTrue(estado is CameraUiState.Error)
        assertEquals("No hay usuario autenticado", (estado as CameraUiState.Error).message)
    }

    @Test
    fun `processCapture con analisis exitoso emite Success`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserId() } returns "uid_123"
        val mockBitmap = mockk<Bitmap>(relaxed = true)

        // Simulamos que el caso de uso devuelve un éxito
        coEvery {
            analyzeCaptureUseCase("uid_123", any(), -33.456, -70.648)
        } returns CaptureResult.Success("Abeja detectada")

        // Act
        viewModel.processCapture(mockBitmap, -33.456, -70.648)

        // Adelantamos el tiempo virtual de la corrutina para que termine de ejecutar
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertTrue(estado is CameraUiState.Success)
        assertEquals("Abeja detectada", (estado as CameraUiState.Success).message)
    }

    @Test
    fun `processCapture con baja probabilidad emite Error formateado`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserId() } returns "uid_123"
        val mockBitmap = mockk<Bitmap>(relaxed = true)

        // Simulamos una probabilidad del 45% (0.45)
        coEvery {
            analyzeCaptureUseCase(any(), any(), any(), any())
        } returns CaptureResult.LowConfidence(0.45)

        // Act
        viewModel.processCapture(mockBitmap, null, null)
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertTrue(estado is CameraUiState.Error)
        val mensajeError = (estado as CameraUiState.Error).message

        // Verificamos que el mensaje contiene la advertencia y el porcentaje calculado
        assertTrue(mensajeError.contains("certeza"))
        assertTrue(mensajeError.contains("45%"))
    }

    @Test
    fun `processCapture falla por excepcion y emite Error generico`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserId() } returns "uid_123"
        val mockBitmap = mockk<Bitmap>(relaxed = true)

        // Simulamos un fallo como falta de internet o error de servidor
        coEvery {
            analyzeCaptureUseCase(any(), any(), any(), any())
        } throws Exception("Timeout de red")

        // Act
        viewModel.processCapture(mockBitmap, null, null)
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertTrue(estado is CameraUiState.Error)
        assertEquals("Error: Timeout de red", (estado as CameraUiState.Error).message)
    }
}