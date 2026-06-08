package com.cetecom.ibichos.presentation.catalog

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import com.cetecom.ibichos.presentation.theme.IBichosTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests de UI para CatalogScreen en aislamiento.
 *
 * Usa HiltTestActivity (en lugar de MainActivity) para evitar pasar por el
 * flujo de splash/navegación. El contenido se monta directamente con setContent.
 *
 * Los repositorios son reemplazados por fakes mediante TestRepositoryModule:
 *   - FakeAuthRepository  → getCurrentUserId() = "test_uid"
 *   - FakeCaptureRepository → devuelve "Abeja" (APPROVED 94%) y "Araña" (REJECTED 88%)
 */
@HiltAndroidTest
class CatalogScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            IBichosTheme {
                val viewModel: CatalogViewModel = hiltViewModel()
                CatalogScreen(
                    viewModel = viewModel,
                    onNavigateToMap = {},
                    onNavigateToDetail = {}
                )
            }
        }

        // Esperar a que las capturas fake terminen de cargarse
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Mi Álbum").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun pantalla_muestraTituloAlbum() {
        composeTestRule.onNodeWithText("Mi Álbum").assertIsDisplayed()
    }

    @Test
    fun pantalla_muestraContadorDeCapturas() {
        // FakeCaptureRepository devuelve 2 capturas
        composeTestRule.onNodeWithText("2 insectos capturados").assertIsDisplayed()
    }

    @Test
    fun botonMapa_existeYEsClickeable() {
        composeTestRule.onNodeWithText("Mapa").assertExists().assertHasClickAction()
    }

    @Test
    fun lista_muestraNombreDeInsectos() {
        composeTestRule.onNodeWithText("Abeja").assertIsDisplayed()
        composeTestRule.onNodeWithText("Araña").assertIsDisplayed()
    }

    @Test
    fun lista_muestraNombreCientifico() {
        composeTestRule.onNodeWithText("Apis mellifera").assertIsDisplayed()
    }

    @Test
    fun lista_muestraPorcentajeDeConfianza() {
        // La captura "Abeja" tiene probability = 0.94 → "94%"
        composeTestRule.onNodeWithText("94%").assertIsDisplayed()
    }

    @Test
    fun capturaRechazada_muestraBadgeRechazada() {
        // La captura "Araña" tiene status = "REJECTED" → badge "RECHAZADA"
        composeTestRule.onNodeWithText("RECHAZADA").assertIsDisplayed()
    }

    @Test
    fun capturas_sonClickeables() {
        // Las tarjetas de captura deben tener acción de click
        composeTestRule.onNodeWithText("Abeja").assertHasClickAction()
    }
}
