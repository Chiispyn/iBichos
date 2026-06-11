package com.cetecom.ibichos.presentation.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.rule.GrantPermissionRule
import com.cetecom.ibichos.HiltTestActivity
import com.cetecom.ibichos.presentation.camera.CameraScreen
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

    @get:Rule(order = 2)
    val grantPermissions: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            IBichosTheme {
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
                        CameraScreen()
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
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Aceptar").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Aceptar").performClick()

        // Sexo
        composeTestRule.onNodeWithText("Sexo").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Hombre").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Hombre").performClick()

        // Contraseña
        composeTestRule.onNode(hasText("Contraseña") and hasSetTextAction())
            .performTextInput("Password123")

        // Confirmar contraseña
        composeTestRule.onNode(hasText("Confirmar contraseña") and hasSetTextAction())
            .performTextInput("Password123")

        // Esperar a que el botón esté habilitado (todos los campos completos)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Crear Cuenta") and hasClickAction() and isEnabled())
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Hacer clic en "Crear Cuenta"
        composeTestRule.onNode(hasText("Crear Cuenta") and hasClickAction())
            .assertIsEnabled()
            .performClick()

        // Verificar que navegó a CameraScreen:
        // RegisterScreen desaparece → estamos en el CameraScreen real del proyecto
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Únete a la comunidad de cazadores")
                .fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText("Únete a la comunidad de cazadores").assertDoesNotExist()
    }
}
