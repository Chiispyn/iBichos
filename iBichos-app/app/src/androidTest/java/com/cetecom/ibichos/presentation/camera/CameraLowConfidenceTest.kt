package com.cetecom.ibichos.presentation.camera

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.rule.GrantPermissionRule
import com.cetecom.ibichos.HiltTestActivity
import com.cetecom.ibichos.fake.FakeInsectRepository
import com.cetecom.ibichos.presentation.theme.IBichosTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class CameraLowConfidenceTest {

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

    @Before
    fun setUp() {
        // 10% < 40% umbral → ProcessCaptureUseCase retorna CaptureResult.LowConfidence(0.10)
        FakeInsectRepository.probability = 0.10

        hiltRule.inject()

        composeTestRule.setContent {
            IBichosTheme {
                val vm: CameraViewModel = hiltViewModel()
                cameraViewModel = vm
                CameraScreen(viewModel = vm)
            }
        }

        composeTestRule.waitForIdle()
    }

    @After
    fun tearDown() {
        FakeInsectRepository.probability = 0.9
    }

    @Test
    fun ia_bajaConfianza_muestraMensajeYReintentarRestauraCamara() {
        // 1. Esperar botón de captura (cámara lista)
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithContentDescription("Capturar insecto")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Capturar insecto").assertIsDisplayed()

        // 2. Simular foto tomada con coordenadas de Santiago
        val mockBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        composeTestRule.runOnUiThread {
            cameraViewModel?.setPreview(mockBitmap, -33.4569, -70.6483)
        }

        // 3. Overlay "¿Analizar este insecto?" visible
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("¿Analizar este insecto?")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("¿Analizar este insecto?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Asegúrate de que se vea nítido").assertIsDisplayed()

        // 4. Click en "Analizar ✨"
        composeTestRule.onNodeWithText("Analizar ✨").performClick()

        // 5. Estado de carga "Identificando insecto..."
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Identificando insecto...")
                .fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("suficiente certeza", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 6. Mensaje de baja confianza (10% → mensaje exacto del ViewModel)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("suficiente certeza", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(
            "La IA no pudo identificar un insecto con suficiente certeza (10%). Intenta con otra foto con mejor iluminación.",
            substring = true
        ).assertIsDisplayed()

        // 7. Botón "Intentar de nuevo" visible
        composeTestRule.onNodeWithText("Intentar de nuevo").assertIsDisplayed()

        // 8. Presionar "Intentar de nuevo" → resetState() → CameraUiState.Idle
        composeTestRule.onNodeWithText("Intentar de nuevo").performClick()

        // 9. Verificar que el mensaje desaparece y la cámara vuelve al estado inicial
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("suficiente certeza", substring = true)
                .fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText("suficiente certeza", substring = true).assertDoesNotExist()
    }
}
