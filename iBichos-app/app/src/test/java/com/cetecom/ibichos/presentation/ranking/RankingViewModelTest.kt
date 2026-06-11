package com.cetecom.ibichos.presentation.ranking

import com.cetecom.ibichos.dispatcher.MainDispatcherRule
import com.cetecom.ibichos.domain.model.GamificationData
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.model.enums.UserLevel
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.profile.GetUserProfileUseCase
import com.cetecom.ibichos.domain.usecase.ranking.GetRankingUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description



class RankingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val getRankingUseCase: GetRankingUseCase = mockk()
    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()

    @Before
    fun setUp() {
        every { authRepository.getCurrentUserId() } returns "user1"
    }

    @Test
    fun `loadRanking XP orders items descendingly`() = runTest {
        // Simular usuarios ya ordenados por XP por el caso de uso
        val mockUsers = listOf(
            UserProfile(uid = "user1", displayName = "Alice", gamification = GamificationData(xp = 1500L, level = UserLevel.AMATEUR)),
            UserProfile(uid = "user2", displayName = "Bob", gamification = GamificationData(xp = 1000L, level = UserLevel.CASUAL))
        )
        coEvery { getRankingUseCase(RankingType.XP) } returns mockUsers

        val viewModel = RankingViewModel(authRepository, getRankingUseCase, getUserProfileUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("Alice", state.items[0].displayName)
        assertEquals("1.500 XP", state.items[0].valueFormatted)
        assertEquals("Bob", state.items[1].displayName)
        assertEquals("1.000 XP", state.items[1].valueFormatted)
        assertEquals(1, state.currentUserRank)
    }

    @Test
    fun `loadRanking XP tie breaker alphabetic order`() = runTest {
        // En caso de empate de XP (ambos con 1000 XP), el repositorio/caso de uso los ordena alfabéticamente
        val mockUsers = listOf(
            UserProfile(uid = "user1", displayName = "Alice", gamification = GamificationData(xp = 1000L, level = UserLevel.CASUAL)),
            UserProfile(uid = "user2", displayName = "Bob", gamification = GamificationData(xp = 1000L, level = UserLevel.CASUAL))
        )
        coEvery { getRankingUseCase(RankingType.XP) } returns mockUsers

        val viewModel = RankingViewModel(authRepository, getRankingUseCase, getUserProfileUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("Alice", state.items[0].displayName)
        assertEquals("Bob", state.items[1].displayName)
        assertEquals(1, state.items[0].rank)
        assertEquals(2, state.items[1].rank)
    }

    @Test
    fun `loadRanking UNIQUE counts species`() = runTest {
        val mockUsers = listOf(
            UserProfile(uid = "user1", displayName = "Alice", gamification = GamificationData(uniqueInsectsCount = 5)),
            UserProfile(uid = "user2", displayName = "Bob", gamification = GamificationData(uniqueInsectsCount = 3))
        )
        coEvery { getRankingUseCase(RankingType.XP) } returns emptyList()
        coEvery { getRankingUseCase(RankingType.UNIQUE) } returns mockUsers

        val viewModel = RankingViewModel(authRepository, getRankingUseCase, getUserProfileUseCase)
        viewModel.loadRanking(RankingType.UNIQUE)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("Alice", state.items[0].displayName)
        assertEquals("5 especies", state.items[0].valueFormatted)
        assertEquals("Bob", state.items[1].displayName)
        assertEquals("3 especies", state.items[1].valueFormatted)
    }

    @Test
    fun `loadRanking MEDALS counts medals`() = runTest {
        val mockUsers = listOf(
            UserProfile(uid = "user1", displayName = "Alice", gamification = GamificationData(medals = listOf("M1", "M2"))),
            UserProfile(uid = "user2", displayName = "Bob", gamification = GamificationData(medals = listOf("M1")))
        )
        coEvery { getRankingUseCase(RankingType.XP) } returns emptyList()
        coEvery { getRankingUseCase(RankingType.MEDALS) } returns mockUsers

        val viewModel = RankingViewModel(authRepository, getRankingUseCase, getUserProfileUseCase)
        viewModel.loadRanking(RankingType.MEDALS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("2 medallas", state.items[0].valueFormatted)
        assertEquals("1 medallas", state.items[1].valueFormatted)
    }
}
