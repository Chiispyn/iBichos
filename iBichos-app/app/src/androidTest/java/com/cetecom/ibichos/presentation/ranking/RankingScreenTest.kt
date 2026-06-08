package com.cetecom.ibichos.presentation.ranking

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
 * Tests de UI para RankingScreen en aislamiento.
 *
 * Usa HiltTestActivity y setContent para evitar pasar por el flujo de navegación.
 * Los repositorios son reemplazados por fakes via TestRepositoryModule:
 *   - FakeUserRepository devuelve 3 usuarios:
 *       rank 1 → "Jugador Alfa"  (Explorador, 1.500 XP)
 *       rank 2 → "Jugador Beta"  (Aficionado, 1.200 XP)
 *       rank 3 → "test_uid"      (Casual, 900 XP) → se muestra como "Tú"
 */
@HiltAndroidTest
class RankingScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            IBichosTheme {
                val viewModel: RankingViewModel = hiltViewModel()
                RankingScreen(viewModel = viewModel)
            }
        }

        // Esperar a que cargue el ranking con los usuarios fake
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Pizarra de Prestigio")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun pantalla_muestraTitulo() {
        composeTestRule.onNodeWithText("Pizarra de Prestigio").assertIsDisplayed()
    }

    @Test
    fun pantalla_muestraSubtitulo() {
        composeTestRule.onNodeWithText("Compite con otros exploradores").assertIsDisplayed()
    }

    @Test
    fun tabs_muestranTresCategorias() {
        composeTestRule.onNodeWithText("Sabios").assertIsDisplayed()
        composeTestRule.onNodeWithText("Especies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Medallas").assertIsDisplayed()
    }

    @Test
    fun botonReglas_existeYEsClickeable() {
        composeTestRule.onNodeWithText("Reglas").assertExists().assertHasClickAction()
    }

    @Test
    fun botonReglas_abreDialogoSistemaDePrestigio() {
        composeTestRule.onNodeWithText("Reglas").performClick()
        composeTestRule.onNodeWithText("Sistema de Prestigio").assertIsDisplayed()
        composeTestRule.onNodeWithText("¡A Explorar!").assertIsDisplayed()
    }

    @Test
    fun dialogoReglas_seCierraAlConfirmar() {
        composeTestRule.onNodeWithText("Reglas").performClick()
        composeTestRule.onNodeWithText("¡A Explorar!").performClick()
        composeTestRule.onNodeWithText("Sistema de Prestigio").assertDoesNotExist()
    }

    @Test
    fun lista_muestraJugadorAlfa() {
        composeTestRule.onNodeWithText("Jugador Alfa").assertExists()
    }

    @Test
    fun lista_muestraXpFormateada() {
        // FakeUserRepository rank 1 → 1500 XP → "1.500 XP"
        composeTestRule.onNodeWithText("1.500 XP").assertExists()
    }

    @Test
    fun lista_muestraNivelDelJugador() {
        // Jugador Alfa tiene UserLevel.EXPLORER → "Explorador"
        composeTestRule.onNodeWithText("Explorador").assertExists()
    }

    @Test
    fun usuarioActual_semuestraComoTu() {
        // test_uid es rank 1 en FakeUserRepository → siempre visible en pantalla
        // useUnmergedTree = true para encontrar el nodo Text exacto sin fusión semántica
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Tú"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Tú"), useUnmergedTree = true).assertExists()
    }

    @Test
    fun tabEspecies_esClickeable() {
        composeTestRule.onNodeWithText("Especies").assertHasClickAction()
    }

    @Test
    fun tabMedallas_esClickeable() {
        composeTestRule.onNodeWithText("Medallas").assertHasClickAction()
    }
}
