package com.cetecom.ibichos.presentation.catalog

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.cetecom.ibichos.HiltTestActivity
import com.cetecom.ibichos.presentation.catalog.viewdata.CaptureViewData
import com.cetecom.ibichos.presentation.theme.IBichosTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests de UI para CaptureDetailScreen en aislamiento.
 *
 * No requiere ViewModel — la pantalla recibe CaptureViewData directamente.
 * Se usan dos capturas fake:
 *   - fakeCaptura       : APPROVED, con ubicación y descripción, userId = "test_uid"
 *   - fakeCapturaAjena  : APPROVED, sin ubicación ni descripción, userId distinto
 *   - fakeCapturaRechazada : REJECTED, userId = "test_uid" → muestra "Apelar Decisión"
 */
@HiltAndroidTest
class CaptureDetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    // ── Capturas fake ────────────────────────────────────────────────────────
    private val fakeCaptura = CaptureViewData(
        id = "cap_1",
        userId = "test_uid",
        imageUrl = "",
        insectName = "Abeja",
        scientificName = "Apis mellifera",
        categoryLabel = "Himenóptero",
        dangerLabel = "Precaución 🟡",
        probabilityFormatted = "94%",
        dateFormatted = "12 may 2026",
        xpAwarded = 50L,
        description = "Insecto polinizador muy común.",
        status = "APPROVED",
        needsReview = false,
        latitude = -33.45,
        longitude = -70.65
    )

    private val fakeCapturaAjena = fakeCaptura.copy(
        id = "cap_2",
        userId = "otro_uid",
        description = "",
        latitude = null,
        longitude = null
    )

    private val fakeCapturaRechazada = fakeCaptura.copy(
        id = "cap_3",
        status = "REJECTED"
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun lanzarPantalla(
        captura: CaptureViewData = fakeCaptura,
        currentUserId: String = "test_uid"
    ) {
        composeTestRule.setContent {
            IBichosTheme {
                CaptureDetailScreen(
                    capture = captura,
                    currentUserId = currentUserId,
                    onNavigateBack = {},
                    onDelete = {},
                    onAppeal = {},
                    onNavigateToMap = { _, _ -> }
                )
            }
        }
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    fun pantalla_muestraTituloDetalle() {
        lanzarPantalla()
        composeTestRule.onNodeWithText("Detalle del Insecto").assertIsDisplayed()
    }

    @Test
    fun pantalla_muestraNombreInsectoEnMayusculas() {
        lanzarPantalla()
        // insectName.uppercase() = "ABEJA"
        composeTestRule.onNodeWithText("ABEJA").assertIsDisplayed()
    }

    @Test
    fun pantalla_muestraNombreCientifico() {
        lanzarPantalla()
        composeTestRule.onNodeWithText("Apis mellifera").assertIsDisplayed()
    }

    @Test
    fun pantalla_muestraDangerLabel() {
        lanzarPantalla()
        composeTestRule.onAllNodes(hasText("Precaución 🟡", substring = true))
            .onFirst().assertExists()
    }

    @Test
    fun pantalla_muestraPorcentajeConfianza() {
        lanzarPantalla()
        composeTestRule.onNodeWithText("94%").assertIsDisplayed()
    }

    @Test
    fun pantalla_muestraXpGanada() {
        lanzarPantalla()
        composeTestRule.onNodeWithText("+50 XP").assertIsDisplayed()
    }

    @Test
    fun pantalla_muestraFechaCaptura() {
        lanzarPantalla()
        composeTestRule.onNodeWithText("12 may 2026").assertExists()
    }

    @Test
    fun botonEliminar_visibleParaPropietario() {
        // currentUserId == capture.userId → botón delete visible
        lanzarPantalla(captura = fakeCaptura, currentUserId = "test_uid")
        composeTestRule.onNodeWithContentDescription("Borrar").assertExists()
    }

    @Test
    fun botonEliminar_noVisibleParaOtroUsuario() {
        // currentUserId != capture.userId → botón delete NO visible
        lanzarPantalla(captura = fakeCaptura, currentUserId = "otro_uid")
        composeTestRule.onNodeWithContentDescription("Borrar").assertDoesNotExist()
    }

    @Test
    fun dialogoEliminar_abreAlHacerClick() {
        lanzarPantalla()
        composeTestRule.onNodeWithContentDescription("Borrar").performClick()
        composeTestRule.onNodeWithText("¿Eliminar captura?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eliminar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun dialogoEliminar_seCierraAlCancelar() {
        lanzarPantalla()
        composeTestRule.onNodeWithContentDescription("Borrar").performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()
        composeTestRule.onNodeWithText("¿Eliminar captura?").assertDoesNotExist()
    }

    @Test
    fun capturaConUbicacion_muestraTarjetaMapa() {
        // fakeCaptura tiene lat/lng → muestra "Ubicación GPS"
        lanzarPantalla()
        composeTestRule.onNodeWithText("Ubicación GPS").assertExists()
        composeTestRule.onNodeWithText("Toca para ver en el mapa").assertExists()
    }

    @Test
    fun capturaSinUbicacion_noMuestraTarjetaMapa() {
        lanzarPantalla(captura = fakeCapturaAjena)
        composeTestRule.onNodeWithText("Ubicación GPS").assertDoesNotExist()
    }

    @Test
    fun capturaConDescripcion_muestraInfoBiologica() {
        lanzarPantalla()
        composeTestRule.onNodeWithText("Información Biológica").assertExists()
        composeTestRule.onNodeWithText("Insecto polinizador muy común.").assertExists()
    }

    @Test
    fun capturaSinDescripcion_noMuestraInfoBiologica() {
        lanzarPantalla(captura = fakeCapturaAjena)
        composeTestRule.onNodeWithText("Información Biológica").assertDoesNotExist()
    }

    @Test
    fun capturaRechazada_propietario_muestraBotonApelar() {
        // status=REJECTED + currentUserId==userId → "Apelar Decisión" visible
        lanzarPantalla(captura = fakeCapturaRechazada, currentUserId = "test_uid")
        composeTestRule.onNodeWithText("Apelar Decisión").assertExists()
    }

    @Test
    fun capturaAprobada_noMuestraBotonApelar() {
        lanzarPantalla(captura = fakeCaptura, currentUserId = "test_uid")
        composeTestRule.onNodeWithText("Apelar Decisión").assertDoesNotExist()
    }
}
