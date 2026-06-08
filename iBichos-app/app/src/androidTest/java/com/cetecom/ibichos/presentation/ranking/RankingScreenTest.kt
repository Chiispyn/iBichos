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
        // test_uid rank 1 → xp=1500 → "1.500 XP"
        // Puede aparecer en el LazyColumn Y en la tarjeta fija del usuario actual,
        // por eso usamos onAllNodes + onFirst para evitar fallo por múltiples matches
        composeTestRule.onAllNodesWithText("1.500 XP").onFirst().assertExists()
    }

    @Test
    fun lista_muestraNivelDelJugador() {
        // test_uid (rank 1) tiene UserLevel.EXPLORER → "Explorador"
        // substring = true + onFirst para manejar fusion semantica y duplicados del pinned card
        composeTestRule.onAllNodes(hasText("Explorador", substring = true)).onFirst().assertExists()
    }

    @Test
    fun usuarioActual_semuestraComoTu() {
        // test_uid es rank 1 → siempre visible en pantalla
        // substring = true porque el árbol merged fusiona "Tú" con otros textos del card
        // onFirst() para no fallar si aparece también en la tarjeta fija (pinned card)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Tú", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(hasText("Tú", substring = true)).onFirst().assertExists()
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
