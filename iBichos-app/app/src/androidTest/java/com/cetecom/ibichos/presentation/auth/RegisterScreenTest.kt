package com.cetecom.ibichos.presentation.auth

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.cetecom.ibichos.HiltTestActivity
import com.cetecom.ibichos.presentation.MainActivity
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
    fun formulario_completo_rellenaTodasLosCampos() {
        // Nombre
        composeTestRule.onNodeWithText("Nombre de cazador")
            .performTextInput("Juan Pérez")

        // Email
        composeTestRule.onNodeWithText("Correo electrónico")
            .performTextInput("juan@gmail.com")

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

        // Crear Cuenta y navegar a CameraScreen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasText("Crear Cuenta") and hasClickAction() and isEnabled())
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Crear Cuenta") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Verificar que onRegisterSuccess fue llamado → en producción navega a CameraScreen
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            navigatedToCameraScreen
        }
        assert(navigatedToCameraScreen) { "onRegisterSuccess no fue disparado: no se navegó a CameraScreen" }
    }
}

@HiltAndroidTest
class RegisterFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun registro_completo_navegaAlHome() {
        // Esperar splash y llegar al login
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty()
        }

        // Navegar a registro
        composeTestRule.onNodeWithText("¿No tienes cuenta? Regístrate").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Únete a la comunidad de cazadores")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Nombre
        composeTestRule.onNodeWithText("Nombre de cazador")
            .performClick()
            .performTextInput("Juan Perez")

        // Email
        composeTestRule.onNodeWithText("Correo electrónico")
            .performClick()
            .performTextInput("juan${System.currentTimeMillis()}@gmail.com")

        // Contraseña
        composeTestRule.onNode(hasText("Contraseña") and hasSetTextAction())
            .performClick()
            .performTextInput("Password123")

        // Confirmar contraseña
        composeTestRule.onNode(hasText("Confirmar contraseña") and hasSetTextAction())
            .performClick()
            .performTextInput("Password123")

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
            composeTestRule.onAllNodes(hasText("Comuna") and isEnabled())
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Comuna") and isEnabled()).performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Arica").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Arica").performClick()

        // Sexo
        composeTestRule.onNodeWithText("Sexo").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Hombre").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Hombre").performClick()

        // Fecha de nacimiento — campo disabled con .clickable, usar performTouchInput
        composeTestRule.onNode(hasText("Fecha de nacimiento"), useUnmergedTree = true)
            .performScrollTo()
            .performTouchInput { click() }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Aceptar").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Aceptar").performClick()

        // Crear cuenta
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasText("Crear Cuenta") and hasClickAction() and isEnabled())
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Crear Cuenta") and hasClickAction())
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        // Verificar que llegó al home (pantalla principal)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Capturar").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Mi Álbum").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
