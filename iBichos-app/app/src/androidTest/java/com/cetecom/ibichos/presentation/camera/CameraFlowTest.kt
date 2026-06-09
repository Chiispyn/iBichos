package com.cetecom.ibichos.presentation.camera

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.rule.GrantPermissionRule
import com.cetecom.ibichos.HiltTestActivity
import com.cetecom.ibichos.presentation.catalog.CatalogScreen
import com.cetecom.ibichos.presentation.catalog.CatalogViewModel
import com.cetecom.ibichos.presentation.theme.IBichosTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class CameraFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @get:Rule(order = 2)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var cameraViewModel: CameraViewModel? = null
    private var navControllerRef: NavController? = null

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            val navController = rememberNavController()
            val camVm: CameraViewModel = hiltViewModel()
            cameraViewModel = camVm
            navControllerRef = navController

            IBichosTheme {
                NavHost(navController = navController, startDestination = "camera") {
                    composable("camera") {
                        CameraScreen(viewModel = camVm)
                    }
                    composable("catalog") {
                        val catalogVm: CatalogViewModel = hiltViewModel()
                        CatalogScreen(
                            viewModel = catalogVm,
                            onNavigateToMap = {},
                            onNavigateToDetail = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun captura_foto_analiza_y_verifica_catalogo() {
        // 1. Esperar pantalla de cámara con el botón de captura
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("Capturar insecto")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Capturar insecto").assertIsDisplayed()
        Thread.sleep(2000)

        // 2. Simular foto tomada con coordenadas de Santiago
        val mockBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        composeTestRule.runOnUiThread {
            cameraViewModel?.setPreview(mockBitmap, -33.4569, -70.6483)
        }
        Thread.sleep(1500)

        // 3. Pantalla de previsualización — confirmación del insecto
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("¿Analizar este insecto?")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("¿Analizar este insecto?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Asegúrate de que se vea nítido").assertIsDisplayed()
        Thread.sleep(2000)

        // 4. Click en "Analizar ✨"
        composeTestRule.onNodeWithText("Analizar ✨").performClick()
        Thread.sleep(1000)

        // 5. Indicador de carga — "Identificando insecto..."
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Identificando insecto...")
                .fetchSemanticsNodes().isNotEmpty()
                || composeTestRule.onAllNodesWithText("Capturar otro")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(1500)

        // 6. Resultado exitoso
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Capturar otro")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Capturar otro").assertIsDisplayed()
        Thread.sleep(2500)

        // 7. Navegar al catálogo
        composeTestRule.runOnUiThread {
            navControllerRef?.navigate("catalog")
        }
        Thread.sleep(1500)

        // 8. Verificar catálogo con capturas guardadas
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Mi Álbum")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Mi Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abeja").assertIsDisplayed()
        Thread.sleep(2500)
    }

    @Test
    fun previsualizar_foto_botonRepetir_restablece_camara() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("Capturar insecto")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(1500)

        // Simular foto tomada
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        composeTestRule.runOnUiThread {
            cameraViewModel?.setPreview(mockBitmap, null, null)
        }
        Thread.sleep(1500)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("¿Analizar este insecto?")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("¿Analizar este insecto?").assertIsDisplayed()
        Thread.sleep(2000)

        // Click en "Repetir" — vuelve a la cámara
        composeTestRule.onNodeWithText("Repetir").performClick()
        Thread.sleep(1500)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("Capturar insecto")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Capturar insecto").assertIsDisplayed()
        Thread.sleep(1500)
    }
}
