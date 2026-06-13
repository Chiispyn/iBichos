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
class CameraAnalysisErrorTest {

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
        // Simular fallo de red en la identificación del insecto
        FakeInsectRepository.shouldFail = true

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
        FakeInsectRepository.shouldFail = false
    }

    @Test
    fun analisis_fallido_muestraError_y_reintento_restauraCamara() {
        // 1. Disparar processCapture directamente con un bitmap mock
        //    Esto simula que el usuario tomó una foto y presionó "Analizar"
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        composeTestRule.runOnUiThread {
            cameraViewModel?.processCapture(mockBitmap, -33.4569, -70.6483)
        }

        // 2. Verificar que aparece el mensaje de error de red
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText(
                FakeInsectRepository.errorMessage,
                substring = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(
            FakeInsectRepository.errorMessage,
            substring = true
        ).assertIsDisplayed()

        // 3. Verificar que el botón "Intentar de nuevo" está visible
        composeTestRule.onNodeWithText("Intentar de nuevo").assertIsDisplayed()

        // 4. Presionar "Intentar de nuevo" → resetState() → CameraUiState.Idle
        composeTestRule.onNodeWithText("Intentar de nuevo").performClick()

        // 5. Verificar que el error desaparece (estado vuelve a Idle)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(
                FakeInsectRepository.errorMessage,
                substring = true
            ).fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText(
            FakeInsectRepository.errorMessage,
            substring = true
        ).assertDoesNotExist()
    }
}
