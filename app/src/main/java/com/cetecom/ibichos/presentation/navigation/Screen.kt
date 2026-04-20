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
    object Map      : Screen("map")

    object OnboardingOne : Screen (route = "onboardingOne")

    object OnboardingTwo: Screen (route = "onboardingTwo")




    // La pantalla de detalles recibe un índice para acceder a la captura desde el catálogo
    // (Se pasa como indice en la lista del ViewModel compartido — evita encoding de objetos)
    object CaptureDetail : Screen("captureDetail/{captureIndex}") {
        fun createRoute(index: Int) = "captureDetail/$index"
    }
}

