package com.cetecom.ibichos.presentation.navigation

/**
 * Define todas las rutas de navegación de la app.
 * Usar objetos sellados permite que el compilador detecte si falta manejar alguna ruta.
 */
sealed class Screen(val route: String) {
    object Splash   : Screen("splash")
    object Login    : Screen("login")
    object Register : Screen("register")
    object Main     : Screen("main")
    object Map : Screen("map?lat={lat}&lng={lng}") {
        fun createRoute(lat: Double, lng: Double) = "map?lat=$lat&lng=$lng"
    }

    object OnboardingOne : Screen (route = "onboardingOne")

    object OnboardingTwo: Screen (route = "onboardingTwo")
    object CompleteProfile : Screen("completeProfile")




    // La pantalla de detalles ahora obtiene la captura seleccionada desde NavigationState
    object CaptureDetail : Screen("captureDetail")

}
