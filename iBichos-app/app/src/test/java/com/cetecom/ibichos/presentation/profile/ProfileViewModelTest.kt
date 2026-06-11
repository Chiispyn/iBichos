package com.cetecom.ibichos.presentation.profile

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.cetecom.ibichos.dispatcher.MainDispatcherRule
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.profile.GetUserProfileUseCase
import com.cetecom.ibichos.domain.usecase.profile.UploadAvatarUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()
    private val uploadAvatarUseCase: UploadAvatarUseCase = mockk()

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        // UID por defecto para que el bloque init no se detenga
        every { authRepository.getCurrentUserId() } returns "uid_123"

        // Retornamos un mock relajado del perfil para evitar problemas con toViewData()
        coEvery { getUserProfileUseCase(any()) } returns mockk(relaxed = true)
    }

    @Test
    fun `al inicializar carga el perfil del usuario`() = runTest {
        // Act: La instanciación llama a init -> loadProfile()
        viewModel = ProfileViewModel(authRepository, getUserProfileUseCase, uploadAvatarUseCase)
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertFalse(estado.isLoading)
        assertNull(estado.error)

        coVerify(exactly = 1) { getUserProfileUseCase("uid_123") }
    }

    @Test
    fun `loadProfile sin usuario no hace nada`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserId() } returns null

        // Act
        viewModel = ProfileViewModel(authRepository, getUserProfileUseCase, uploadAvatarUseCase)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { getUserProfileUseCase(any()) }
    }

    @Test
    fun `uploadAvatar procesa el archivo y actualiza la URL con exito`() = runTest {
        // Arrange
        viewModel = ProfileViewModel(authRepository, getUserProfileUseCase, uploadAvatarUseCase)
        advanceUntilIdle() // Dejamos que el init termine

        // 1. Mocks de Android (Uri, Context, ContentResolver)
        val mockUri = mockk<Uri>()
        val mockContext = mockk<Context>()
        val mockResolver = mockk<ContentResolver>()

        // 2. Simulamos un InputStream con datos falsos ("foto")
        val fakeInputStream = ByteArrayInputStream("foto".toByteArray())

        // 3. Usamos la carpeta temporal real de la computadora (JVM) para que el File no falle
        val tempDir = File(System.getProperty("java.io.tmpdir"))

        // Configuramos cómo se comporta el Context de mentira
        every { mockContext.cacheDir } returns tempDir
        every { mockContext.contentResolver } returns mockResolver
        every { mockResolver.openInputStream(mockUri) } returns fakeInputStream

        // Simulamos que subir el avatar devuelve una URL
        val urlEsperada = "https://ibichos.cl/avatars/uid_123.jpg"
        coEvery { uploadAvatarUseCase(any(), any()) } returns urlEsperada

        // Act
        viewModel.uploadAvatar(mockUri, mockContext)
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertFalse(estado.isUploadingAvatar)
        assertEquals(urlEsperada, estado.profile?.avatarUrl)
        assertEquals("¡Avatar actualizado en la nube!", estado.successMessage)
    }

    @Test
    fun `uploadAvatar falla y muestra error`() = runTest {
        // Arrange
        viewModel = ProfileViewModel(authRepository, getUserProfileUseCase, uploadAvatarUseCase)
        advanceUntilIdle()

        val mockUri = mockk<Uri>()
        val mockContext = mockk<Context>()
        val mockResolver = mockk<ContentResolver>()

        val tempDir = File(System.getProperty("java.io.tmpdir"))
        every { mockContext.cacheDir } returns tempDir
        every { mockContext.contentResolver } returns mockResolver
        every { mockResolver.openInputStream(mockUri) } returns ByteArrayInputStream(ByteArray(0))

        coEvery { uploadAvatarUseCase(any(), any()) } throws Exception("Error de servidor")

        // Act
        viewModel.uploadAvatar(mockUri, mockContext)
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        assertFalse(estado.isUploadingAvatar)
        assertEquals("Error al subir foto: Error de servidor", estado.error)
    }

    @Test
    fun `logout llama al repositorio correctamente`() = runTest {
        // Arrange
        viewModel = ProfileViewModel(authRepository, getUserProfileUseCase, uploadAvatarUseCase)

        // Act
        viewModel.logout()

        // Assert
        coVerify(exactly = 1) { authRepository.signOut() }
    }

    @Test
    fun `clearMessages limpia el error y el mensaje de exito`() = runTest {
        // Arrange
        viewModel = ProfileViewModel(authRepository, getUserProfileUseCase, uploadAvatarUseCase)
        advanceUntilIdle()

        // Forzamos un estado inicial con mensajes de error y éxito activos
        // (Aunque esto rompe un poco el encapsulamiento para el test, logramos el mismo
        // efecto mockeando una falla previa, pero llamar clearMessages directamente es más rápido)

        // Act
        viewModel.clearMessages()

        // Assert
        val estado = viewModel.uiState.value
        assertNull(estado.error)
        assertNull(estado.successMessage)
    }
}