package com.cetecom.ibichos.presentation.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import com.cetecom.ibichos.HiltTestActivity
import com.cetecom.ibichos.presentation.theme.IBichosTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests de UI para ProfileScreen en aislamiento.
 *
 * Usa HiltTestActivity + setContent para evitar el flujo de navegación.
 * Los repositorios son reemplazados por fakes via TestRepositoryModule:
 *   - FakeAuthRepository  → getCurrentUserId() = "test_uid"
 *   - FakeUserRepository  → getUserProfile("test_uid"):
 *       displayName = "Jugador Test"
 *       xp          = 1500  → "1500" en stats, "1500 / 5000 XP" en progreso
 *       level       = EXPLORER → "Explorador"
 *       medals      = [FIRST_CAPTURE, ARACHNOLOGIST]
 *                     → "Primer Avistamiento", "Aracnólogo"
 */
@HiltAndroidTest
class ProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            IBichosTheme {
                val viewModel: ProfileViewModelTest = hiltViewModel()
                ProfileScreen(
                    onLogout = {},
                    viewModel = viewModel
                )
            }
        }
        // Esperar a que el perfil cargue desde FakeUserRepository
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Jugador Test")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    // ── Header ───────────────────────────────────────────────────────────────

    @Test
    fun header_muestraNombreUsuario() {
        composeTestRule.onNodeWithText("Jugador Test").assertIsDisplayed()
    }

    @Test
    fun header_muestraTextoTocaParaCambiarAvatar() {
        composeTestRule.onNodeWithText("Toca para cambiar avatar").assertIsDisplayed()
    }

    // ── Stats card ───────────────────────────────────────────────────────────

    @Test
    fun stats_muestraXpTotal() {
        // FakeUserRepository xp=1500 → se renderiza como "$xp" = "1500"
        composeTestRule.onAllNodes(hasText("1500", substring = true)).onFirst().assertExists()
    }

    @Test
    fun stats_muestraNivel() {
        // UserLevel.EXPLORER → "Explorador"
        composeTestRule.onAllNodes(hasText("Explorador", substring = true)).onFirst().assertExists()
    }

    @Test
    fun stats_muestraEtiquetaXpTotal() {
        composeTestRule.onNodeWithText("XP Total").assertExists()
    }

    @Test
    fun stats_muestraEtiquetaNivel() {
        composeTestRule.onNodeWithText("Nivel").assertExists()
    }

    @Test
    fun stats_muestraEtiquetaCapturas() {
        composeTestRule.onNodeWithText("Capturas").assertExists()
    }

    // ── Progress card ────────────────────────────────────────────────────────

    @Test
    fun progreso_muestraTituloProgresoNivel() {
        composeTestRule.onNodeWithText("Progreso de nivel").assertExists()
    }

    @Test
    fun progreso_muestraSubtituloExplorando() {
        composeTestRule.onNodeWithText("Sigue explorando para subir de nivel").assertExists()
    }

    @Test
    fun progreso_muestraXpSobreMaximo() {
        // xp=1500, getNextLevelXp(1500) = 5000 → "1500 / 5000 XP"
        composeTestRule.onNodeWithText("1500 / 5000 XP").assertExists()
    }

    // ── Achievements card ────────────────────────────────────────────────────

    @Test
    fun logros_muestraTituloLogrosDesbloqueados() {
        // medals.size = 2 > 0 → AchievementsCard visible
        composeTestRule.onNodeWithText("Logros Desbloqueados").assertExists()
    }

    @Test
    fun logros_muestraMedallaPrimerAvistamiento() {
        composeTestRule.onNodeWithText("Primer Avistamiento").assertExists()
    }

    @Test
    fun logros_muestraMedallaAracnologo() {
        composeTestRule.onAllNodes(hasText("Aracnólogo", substring = true)).onFirst().assertExists()
    }

    @Test
    fun logros_clickMedallaAbreDialogo() {
        composeTestRule.onNodeWithText("Primer Avistamiento").performClick()
        composeTestRule.onNodeWithText("Genial").assertIsDisplayed()
    }

    @Test
    fun dialogoMedalla_seCierraAlConfirmar() {
        composeTestRule.onNodeWithText("Primer Avistamiento").performClick()
        composeTestRule.onNodeWithText("Genial").performClick()
        composeTestRule.onNodeWithText("Genial").assertDoesNotExist()
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @Test
    fun botonCerrarSesion_existeYEsClickeable() {
        composeTestRule.onNodeWithText("Cerrar Sesión").assertExists().assertHasClickAction()
    }
}
