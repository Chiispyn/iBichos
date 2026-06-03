package com.cetecom.ibichos.presentation.map

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.map.GetMapCapturesUseCase
import com.cetecom.ibichos.presentation.ranking.MainDispatcherRule
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

class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val getMapCapturesUseCase: GetMapCapturesUseCase = mockk()

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user1"
    }

    @Test
    fun `loadCaptures filters out null or invalid coordinate captures`() = runTest {
        val mockCaptures = listOf(
            // Válido
            CaptureItem(id = "c1", insectName = "Abeja", category = InsectCategory.HYMENOPTERA, latitude = -33.456, longitude = -70.648),
            // Latitud nula
            CaptureItem(id = "c2", insectName = "Araña", category = InsectCategory.ARACHNID, latitude = null, longitude = -70.648),
            // Longitud nula
            CaptureItem(id = "c3", insectName = "Mariposa", category = InsectCategory.LEPIDOPTERA, latitude = -33.456, longitude = null),
            // Fuera de límites latitud
            CaptureItem(id = "c4", insectName = "Escarabajo", category = InsectCategory.COLEOPTERA, latitude = 95.0, longitude = -70.648),
            // Fuera de límites longitud
            CaptureItem(id = "c5", insectName = "Libélula", category = InsectCategory.OTHER, latitude = -33.456, longitude = 200.0)
        )

        coEvery { getMapCapturesUseCase("user1", false) } returns mockCaptures

        val viewModel = MapViewModel(authRepository, getMapCapturesUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.captures.size)
        assertEquals("c1", state.captures[0].id)
        assertEquals("Abeja", state.captures[0].insectName)
        assertEquals(-33.456, state.captures[0].latitude, 0.001)
        assertEquals(-70.648, state.captures[0].longitude, 0.001)
    }

    @Test
    fun `setGlobalMode triggers reload with isGlobal true`() = runTest {
        val userCaptures = listOf(
            CaptureItem(id = "c1", insectName = "Abeja", category = InsectCategory.HYMENOPTERA, latitude = -33.456, longitude = -70.648)
        )
        val globalCaptures = listOf(
            CaptureItem(id = "c1", insectName = "Abeja", category = InsectCategory.HYMENOPTERA, latitude = -33.456, longitude = -70.648),
            CaptureItem(id = "c2", insectName = "Mariposa", category = InsectCategory.LEPIDOPTERA, latitude = -30.0, longitude = -71.0)
        )

        coEvery { getMapCapturesUseCase("user1", false) } returns userCaptures
        coEvery { getMapCapturesUseCase("user1", true) } returns globalCaptures

        val viewModel = MapViewModel(authRepository, getMapCapturesUseCase)
        advanceUntilIdle()

        // Por defecto no es global
        assertEquals(1, viewModel.uiState.value.captures.size)

        // Cambiar a global
        viewModel.setGlobalMode(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isGlobalMap)
        assertEquals(2, viewModel.uiState.value.captures.size)
    }
}
