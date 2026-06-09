package com.cetecom.ibichos.presentation.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            IBichosTheme {
                // NavHost con Register → pantalla destino "camera" (sin CameraX)
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "register") {
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate("camera") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onNavigateBack = {}
                        )
                    }
                    composable("camera") {
                        // Pantalla destino — sin CameraX para no crashear HiltTestActivity
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Pantalla de Cámara")
                        }
                    }
                }
            }
        }
        // Esperar a que RegisterScreen esté lista
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

        // Esperar a que el botón esté habilitado (todos los campos completos)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Crear Cuenta") and hasClickAction() and isEnabled())
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Hacer clic en "Crear Cuenta"
        composeTestRule.onNode(hasText("Crear Cuenta") and hasClickAction())
            .assertIsEnabled()
            .performScrollTo()
            .performClick()

        // Verificar que la UI navegó a CameraScreen
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Pantalla de Cámara")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Pantalla de Cámara").assertIsDisplayed()
    }
}
