package com.cetecom.ibichos.presentation.auth


import com.cetecom.ibichos.dispatcher.MainDispatcherRule
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.auth.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    // Reemplaza el hilo principal por el de pruebas
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocks de todas las dependencias inyectadas en el constructor
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val loginUseCase: LoginUseCase = mockk()
    private val registerUseCase: RegisterUseCase = mockk()
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk()
    private val completeProfileUseCase: CompleteProfileUseCase = mockk()
    private val getLocationsUseCase: GetLocationsUseCase = mockk()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        // 1. Configuramos el comportamiento por defecto necesario para el bloque "init"
        every { authRepository.getCurrentUserId() } returns null
        coEvery { getLocationsUseCase() } returns mapOf("Biobío" to listOf("Concepción", "Florida"))

        // 2. Inicializamos el ViewModel
        viewModel = AuthViewModel(
            authRepository,
            loginUseCase,
            registerUseCase,
            signInWithGoogleUseCase,
            completeProfileUseCase,
            getLocationsUseCase
        )
    }

    @Test
    fun `init carga ubicaciones correctamente`() = runTest {
        // Act: El init ya se ejecutó en el setup()
        val locacionesActuales = viewModel.locations.value

        // Assert
        assertTrue(locacionesActuales.containsKey("Biobío"))
        assertEquals(2, locacionesActuales["Biobío"]?.size)
    }

    @Test
    fun `login con campos vacios emite error inmediatamente`() = runTest {
        // Act
        viewModel.login("", "123456")

        // Assert
        val estado = viewModel.uiState.value
        assertEquals("Completá todos los campos", estado.error)
        assertFalse(estado.isLoading)
    }

    @Test
    fun `login exitoso emite estado de exito al finalizar`() = runTest {
        // Arrange
        coEvery { loginUseCase("test@ibichos.com", "clave123") } returns Unit
        every { authRepository.getCurrentUserId() } returns "usuario_abc_123"

        // Act
        viewModel.login("test@ibichos.com", "clave123")
        advanceUntilIdle() // Esperamos a que la corrutina termine su trabajo relámpago

        // Assert: Evaluamos directamente el estado final
        val estadoFinal = viewModel.uiState.value
        assertFalse(estadoFinal.isLoading)
        assertTrue(estadoFinal.isSuccess)
        assertEquals("usuario_abc_123", estadoFinal.userId)
        assertEquals(null, estadoFinal.error)
    }

    @Test
    fun `login fallido captura la excepcion y emite error`() = runTest {
        // Arrange: Simulamos que Firebase tira un error
        val mensajeError = "Contraseña incorrecta"
        coEvery { loginUseCase(any(), any()) } throws Exception(mensajeError)

        val estadosEmitidos = mutableListOf<AuthUiState>()
        val recolectorJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { estadosEmitidos.add(it) }
        }

        // Act
        viewModel.login("test@ibichos.com", "claveIncorrecta")

        // Assert
        val estadoFinal = estadosEmitidos.last()
        assertFalse(estadoFinal.isLoading)
        assertFalse(estadoFinal.isSuccess)
        assertEquals(mensajeError, estadoFinal.error)

        recolectorJob.cancel()
    }
}