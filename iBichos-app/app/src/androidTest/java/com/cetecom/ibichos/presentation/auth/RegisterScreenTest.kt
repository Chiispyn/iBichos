package com.cetecom.ibichos.presentation.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.cetecom.ibichos.HiltTestActivity
import com.cetecom.ibichos.presentation.theme.IBichosTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class RegisterScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    // Flag para verificar que onRegisterSuccess fue disparado (→ navegaría a CameraScreen)
    // @Volatile garantiza visibilidad entre el hilo de Compose y el hilo del test
    @Volatile
    private var navigatedToCameraScreen = false

    @Before
    fun setUp() {
        hiltRule.inject()
        navigatedToCameraScreen = false
        composeTestRule.setContent {
            IBichosTheme {
                RegisterScreen(
                    onRegisterSuccess = { navigatedToCameraScreen = true },
                    onNavigateBack = {}
                )
            }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Únete a la comunidad de cazadores")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }


    @Test
    fun botonCrearCuenta_navegaACameraScreen() {
        // Nombre
        composeTestRule.onNodeWithText("Nombre de cazador")
            .performTextInput("Ana García")

        // Email
        composeTestRule.onNodeWithText("Correo electrónico")
            .performTextInput("ana@gmail.com")

        // Región
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Región").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Región").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Arica y Parinacota").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Arica y Parinacota").performClick()

        // Comuna
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasText("Comuna") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Comuna") and isEnabled()).performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Arica").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Arica").performClick()

        // Fecha de nacimiento
        composeTestRule.onNode(hasText("Fecha de nacimiento"), useUnmergedTree = true)
            .performScrollTo()
            .performTouchInput { click() }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Aceptar").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Aceptar").performClick()

        // Sexo
        composeTestRule.onNodeWithText("Sexo").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Hombre").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Hombre").performClick()

        // Contraseña
        composeTestRule.onNode(hasText("Contraseña") and hasSetTextAction())
            .performScrollTo()
            .performTextInput("Password123")

        // Confirmar contraseña
        composeTestRule.onNode(hasText("Confirmar contraseña") and hasSetTextAction())
            .performScrollTo()
            .performTextInput("Password123")

        // Verificar que el botón "Crear Cuenta" existe y está habilitado
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasText("Crear Cuenta") and hasClickAction() and isEnabled())
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Crear Cuenta") and hasClickAction())
            .assertExists()
            .assertIsEnabled()
            .performScrollTo()
            .performClick()

        // Al hacer clic en "Crear Cuenta" debe dispararse onRegisterSuccess → navegación a CameraScreen
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            navigatedToCameraScreen
        }
        assert(navigatedToCameraScreen) {
            "El botón 'Crear Cuenta' no disparó onRegisterSuccess: no se navegó a CameraScreen"
        }
    }
}
