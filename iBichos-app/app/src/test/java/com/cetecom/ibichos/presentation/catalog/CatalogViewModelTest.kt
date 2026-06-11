package com.cetecom.ibichos.presentation.catalog

import com.cetecom.ibichos.dispatcher.MainDispatcherRule
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.cetecom.ibichos.domain.usecase.capture.DeleteCaptureUseCase
import com.cetecom.ibichos.domain.usecase.capture.GetCapturesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    // Usamos tu regla desde la nueva ruta correcta
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocks
    private val authRepository: AuthRepository = mockk()
    private val captureRepository: CaptureRepository = mockk()
    private val getCapturesUseCase: GetCapturesUseCase = mockk()
    private val deleteCaptureUseCase: DeleteCaptureUseCase = mockk()

    private lateinit var viewModel: CatalogViewModel

    @Before
    fun setup() {
        // Configuramos un comportamiento base seguro para que el "init" no falle
        every { authRepository.getCurrentUserId() } returns "uid_123"

        // Devolvemos una lista vacía para evitar problemas con la función de extensión .toViewDataList()
        coEvery { getCapturesUseCase(any()) } returns emptyList()
    }

    @Test
    fun `al inicializar carga las capturas del usuario correctamente`() = runTest {
        // Act: Instanciamos el ViewModel (esto dispara el init -> loadCaptures)
        viewModel = CatalogViewModel(authRepository, captureRepository, getCapturesUseCase, deleteCaptureUseCase)
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertFalse(estado.isLoading)
        assertEquals(null, estado.error)
        assertTrue(estado.captures.isEmpty()) // Porque mockeamos que devuelve emptyList()

        // Verificamos que se llamó al caso de uso con el UID correcto
        coVerify(exactly = 1) { getCapturesUseCase("uid_123") }
    }

    @Test
    fun `si no hay usuario logueado loadCaptures no hace nada`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserId() } returns null

        // Act
        viewModel = CatalogViewModel(authRepository, captureRepository, getCapturesUseCase, deleteCaptureUseCase)
        advanceUntilIdle()

        // Assert: Verificamos que NO se llamó al caso de uso
        coVerify(exactly = 0) { getCapturesUseCase(any()) }
    }

    @Test
    fun `loadCaptures falla y actualiza el estado con mensaje de error`() = runTest {
        // Arrange: Simulamos error en la BD
        val mensajeError = "Error de red"
        coEvery { getCapturesUseCase(any()) } throws Exception(mensajeError)

        // Act
        viewModel = CatalogViewModel(authRepository, captureRepository, getCapturesUseCase, deleteCaptureUseCase)
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertFalse(estado.isLoading)
        assertTrue(estado.error?.contains(mensajeError) == true)
    }

    @Test
    fun `deleteCapture exitoso recarga la lista de capturas`() = runTest {
        // Arrange
        viewModel = CatalogViewModel(authRepository, captureRepository, getCapturesUseCase, deleteCaptureUseCase)
        advanceUntilIdle() // Esperamos a que termine el init

        // Simulamos que borrar funciona
        coEvery { deleteCaptureUseCase("captura_1") } returns Unit

        // Act
        viewModel.deleteCapture("captura_1")
        advanceUntilIdle()

        // Assert
        // getCapturesUseCase se debió llamar 2 veces: 1 en el init, y 1 después de borrar
        coVerify(exactly = 2) { getCapturesUseCase("uid_123") }
        coVerify(exactly = 1) { deleteCaptureUseCase("captura_1") }
    }

    @Test
    fun `appealCapture falla y muestra mensaje de error`() = runTest {
        // Arrange
        viewModel = CatalogViewModel(authRepository, captureRepository, getCapturesUseCase, deleteCaptureUseCase)
        advanceUntilIdle()

        val mensajeFallo = "Servidor no responde"
        coEvery { captureRepository.appealCapture("captura_2") } throws Exception(mensajeFallo)

        // Act
        viewModel.appealCapture("captura_2")
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertEquals("No se pudo apelar: $mensajeFallo", estado.error)
    }
}