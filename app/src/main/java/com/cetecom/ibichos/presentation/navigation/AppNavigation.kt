package com.cetecom.ibichos.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.cetecom.ibichos.presentation.auth.AuthViewModel
import com.cetecom.ibichos.presentation.auth.LoginScreen
import com.cetecom.ibichos.presentation.auth.RegisterScreen
import com.cetecom.ibichos.presentation.camera.CameraScreen
import com.cetecom.ibichos.presentation.catalog.CaptureDetailScreen
import com.cetecom.ibichos.presentation.catalog.CatalogScreen
import com.cetecom.ibichos.presentation.catalog.CatalogViewModel
import com.cetecom.ibichos.presentation.map.MapScreen
import com.cetecom.ibichos.presentation.onboarding.IBichosWelcomeScreen
import com.cetecom.ibichos.presentation.onboarding.IBichosWelcomeTwoScreen
import com.cetecom.ibichos.presentation.profile.ProfileScreen
import com.cetecom.ibichos.presentation.ranking.RankingScreen
import com.cetecom.ibichos.presentation.splash.SplashScreen
import com.cetecom.ibichos.ui.theme.*

// ── Ítems del BottomNav ───────────────────────────────────────────────────────
private data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("Cámara",   "camera",  Icons.Filled.Camera,               Icons.Outlined.Camera),
    BottomNavItem("Álbum",    "catalog", Icons.Filled.CollectionsBookmark,   Icons.Outlined.CollectionsBookmark),
    BottomNavItem("Ranking", "ranking", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    BottomNavItem("Perfil",   "profile", Icons.Filled.Person,               Icons.Outlined.Person)
)

/**
 * NavHost raíz de la aplicación.
 * Maneja: Login → Register → Main (con BottomNav) → CaptureDetail → Map
 */
@Composable
fun AppNavigation() {
    val navController     = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val startDestination  = Screen.Splash.route

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        enterTransition  = { fadeIn() },
        exitTransition   = { fadeOut() }
    ) {
        // ── Splash ────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val nextRoute = if (authViewModel.isLoggedIn()) Screen.Main.route else Screen.Login.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Auth ──────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel        = authViewModel,
                onLoginSuccess   = {
                    navController.navigate(Screen.OnboardingOne.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            // Cada pantalla de registro tiene su propio ViewModel para estado separado
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateBack    = { navController.popBackStack() }
            )
        }

        // ── Main (BottomNav) ──────────────────────────────────────────────
        composable(Screen.Main.route) {
            MainScreenWithBottomNav(
                onNavigateToDetail = { index: Int ->
                    navController.navigate(Screen.CaptureDetail.createRoute(index))
                },
                onNavigateToMap    = { navController.navigate(Screen.Map.route) },
                onLogout           = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Detalle de captura ────────────────────────────────────────────
        // El ViewModel de catálogo es compartido (misma instancia en Main y Detail)
        composable(
            route     = Screen.CaptureDetail.route,
            arguments = listOf(navArgument("captureIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("captureIndex") ?: 0
            val catalogViewModel: CatalogViewModel = viewModel(
                viewModelStoreOwner = navController.getBackStackEntry(Screen.Main.route)
            )
            val state by catalogViewModel.uiState.collectAsState()
            val capture = state.captures.getOrNull(index)

            if (capture != null) {
                CaptureDetailScreen(
                    capture        = capture,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Si la lista aún no cargó, mostrar loading
                androidx.compose.foundation.layout.Box(
                    modifier            = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment    = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(color = IBichosGreen)
                }
            }
        }

        // ── Mapa ─────────────────────────────────────────────────────────
        composable(Screen.Map.route) {
            MapScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.OnboardingOne.route){
            IBichosWelcomeScreen(
                onStartClick = {
                 navController.navigate(Screen.OnboardingTwo.route)
                }
            )

        }
        composable(route = Screen.OnboardingTwo.route){
            IBichosWelcomeTwoScreen(
                onStartClick = {
                    navController.navigate(Screen.Main.route)
                }
            )
        }
    }
}
/**
 * Pantalla principal con NavigationBar inferior y sub-NavHost para las 3 tabs.
 */
@Composable
fun MainScreenWithBottomNav(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToMap: () -> Unit,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry   by bottomNavController.currentBackStackEntryAsState()
    val currentRoute        = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            if (!selected) {
                                bottomNavController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState    = true
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        },
                        icon     = {
                            Icon(
                                if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label    = { Text(item.label) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = IBichosGreen,
                            selectedTextColor   = IBichosGreen,
                            indicatorColor      = IBichosGreenDim,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = bottomNavController,
            startDestination = "camera",
            modifier         = Modifier.padding(paddingValues),
            enterTransition  = { fadeIn() },
            exitTransition   = { fadeOut() }
        ) {
            composable("camera") {
                CameraScreen()
            }
            composable("catalog") {
                // viewModel() aquí usa el ViewModelStoreOwner de este NavBackStackEntry
                val catalogViewModel: CatalogViewModel = viewModel()
                CatalogScreen(
                    viewModel          = catalogViewModel,
                    onNavigateToMap    = onNavigateToMap,
                    onNavigateToDetail = { capture ->
                        val index = catalogViewModel.uiState.value.captures.indexOf(capture)
                        if (index >= 0) onNavigateToDetail(index)
                    }
                )
            }
            composable("profile") {
                ProfileScreen(onLogout = onLogout)
            }
            composable("ranking") {
                RankingScreen(
                )
            }
        }
    }
}

