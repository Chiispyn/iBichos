package com.cetecom.ibichos.presentation.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.rule.GrantPermissionRule
import com.cetecom.ibichos.HiltTestActivity
import com.cetecom.ibichos.fake.FakeAuthRepository
import com.cetecom.ibichos.presentation.theme.IBichosTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class RegisterEmailExisteTest {

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
        // Simular que el correo ya existe en la base de datos
        FakeAuthRepository.shouldFailRegister = true
        FakeAuthRepository.registerErrorMessage = "El correo electrónico ya está registrado"

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
                            onNavigateBack = {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("camera") {
                        // destino alternativo (no se alcanza en este test)
                    }
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {},
                            onNavigateToRegister = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Únete a la comunidad de cazadores")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @After
    fun tearDown() {
        FakeAuthRepository.shouldFailRegister = false
    }

    @Test
    fun correoYaExiste_muestraErrorYNavegaALogin() {
        // Nombre
        composeTestRule.onNodeWithText("Nombre de cazador")
            .performTextInput("Ana García")

        // Email
        composeTestRule.onNodeWithText("Correo electrónico")
            .performTextInput("ana@gmail.com")

        // Región
        composeTestRule.waitUntil(timeoutMillis = 7000) {
            composeTestRule.onAllNodesWithText("Región").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Región").performClick()
        composeTestRule.waitUntil(timeoutMillis = 7000) {
            composeTestRule.onAllNodesWithText("Arica y Parinacota").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Arica y Parinacota").performClick()

        // Comuna
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Comuna") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Comuna") and isEnabled()).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Arica").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Arica").performClick()

        // Fecha de nacimiento
        composeTestRule.onNode(hasText("Fecha de nacimiento"), useUnmergedTree = true)
            .performScrollTo()
            .performTouchInput { click() }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Aceptar").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Aceptar").performClick()

        // Sexo
        composeTestRule.onNodeWithText("Sexo").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
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

        // Esperar a que el botón esté habilitado
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodes(hasText("Crear Cuenta") and hasClickAction() and isEnabled())
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Presionar "Crear Cuenta" — debe fallar porque el correo ya existe
        composeTestRule.onNode(hasText("Crear Cuenta") and hasClickAction())
            .assertIsEnabled()
            .performScrollTo()
            .performClick()

        // Verificar que aparece el error de correo ya registrado
        composeTestRule.waitUntil(timeoutMillis = 7000) {
            composeTestRule.onAllNodesWithText(
                "El correo electrónico ya está registrado",
                substring = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(
            "El correo electrónico ya está registrado",
            substring = true
        ).assertIsDisplayed()

        // Presionar "¿Ya tienes una cuenta? Iniciar sesión" → debe navegar al login
        composeTestRule.onNodeWithText("¿Ya tienes una cuenta? Iniciar sesión")
            .performScrollTo()
            .performClick()

        // Verificar que navegó al LoginScreen real
        composeTestRule.waitUntil(timeoutMillis = 11000) {
            composeTestRule.onAllNodesWithText("Caza, colecciona y explora")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Caza, colecciona y explora").assertIsDisplayed()
    }
}
