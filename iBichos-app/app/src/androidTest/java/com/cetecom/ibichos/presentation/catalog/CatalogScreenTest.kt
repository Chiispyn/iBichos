package com.cetecom.ibichos.presentation.catalog

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.cetecom.ibichos.presentation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests de UI para CatalogScreen (tab "Álbum").
 *
 * Usa fakes de AuthRepository y CaptureRepository inyectados por TestRepositoryModule:
 *   - FakeAuthRepository simula un usuario autenticado con perfil completo.
 *   - FakeCaptureRepository devuelve 2 capturas predefinidas ("Abeja" aprobada y "Araña" rechazada).
 *
 * Flujo: Splash → (isLoggedIn=true) → Main → click tab "Álbum" → CatalogScreen.
 */
@HiltAndroidTest
class CatalogScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()

        // Esperar a que el splash termine y aparezca la bottom nav (usuario autenticado)
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("Álbum").fetchSemanticsNodes().isNotEmpty()
        }

        // Navegar al tab de Álbum (CatalogScreen)
        composeTestRule.onNodeWithText("Álbum").performClick()

        // Esperar a que cargue el catálogo con las capturas fake
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
        // La captura "Abeja" tiene probability=0.94 → "94%"
        composeTestRule.onNodeWithText("94%").assertIsDisplayed()
    }

    @Test
    fun capturaRechazada_muestraBadgeRechazada() {
        // La captura "Araña" tiene status="REJECTED" → muestra badge "RECHAZADA"
        composeTestRule.onNodeWithText("RECHAZADA").assertIsDisplayed()
    }

    @Test
    fun capturas_sonClickeables() {
        // Las tarjetas de captura deben tener acción de click
        composeTestRule.onNodeWithText("Abeja").assertHasClickAction()
    }
}
