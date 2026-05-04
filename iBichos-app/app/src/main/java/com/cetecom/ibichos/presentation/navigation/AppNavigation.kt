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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.cetecom.ibichos.data.OnboardingPreferences
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.presentation.auth.AuthViewModel
import com.cetecom.ibichos.presentation.auth.CompleteProfileScreen
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch


object NavigationState {
    var captureForDetail: CaptureItem? = null
}

// ── Ítems del BottomNav ───────────────────────────────────────────────────────
private data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("Cámara", "camera", Icons.Filled.Camera, Icons.Outlined.Camera),
    BottomNavItem("Álbum", "catalog", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    BottomNavItem("Ranking", "ranking", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    BottomNavItem("Perfil", "profile", Icons.Filled.Person, Icons.Outlined.Person)
)

/*──────────────── AppNavigation ────────────────*/
@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val onboardingPrefs = remember { OnboardingPreferences(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {

        /*──────── Splash ────────*/
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (authViewModel.isLoggedIn()) {
                        authViewModel.checkProfileCompletion { isComplete ->
                            val nextRoute = if (isComplete) Screen.Main.route else Screen.CompleteProfile.route
                            navController.navigate(nextRoute) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        /*──────── Login ────────*/
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {

                    val nextRoute =
                        if (onboardingPrefs.isOnboardingSeen())
                            Screen.Main.route
                        else
                            Screen.OnboardingOne.route

                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },

                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        /*──────── Register ────────*/
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.OnboardingOne.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        /*──────── Main ────────*/
        composable(Screen.Main.route) {
            MainScreenWithBottomNav(
                onNavigateToDetail = { capture ->
                    NavigationState.captureForDetail = capture
                    navController.navigate(Screen.CaptureDetail.route)
                },
                onNavigateToMap    = { navController.navigate(Screen.Map.route) },
                onLogout           = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Detalle de captura ────────────────────────────────────────────
        composable(Screen.CaptureDetail.route) {
            val capture = NavigationState.captureForDetail

            if (capture != null) {
                val scope = androidx.compose.runtime.rememberCoroutineScope()

                CaptureDetailScreen(
                    capture        = capture,
                    onNavigateBack = { navController.popBackStack() },
                    onDelete = { id ->
                        scope.launch {
                            com.cetecom.ibichos.data.repository.CaptureRepositoryImpl().deleteCapture(id)
                            navController.popBackStack()
                        }
                    },
                    onNavigateToMap = { lat, lng ->
                        navController.navigate(Screen.Map.createRoute(lat, lng))
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        // ── Mapa ─────────────────────────────────────────────────────────
        composable(
            route = Screen.Map.route,
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType; nullable = true },
                navArgument("lng") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()

            MapScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { capture ->
                    NavigationState.captureForDetail = capture
                    navController.navigate(Screen.CaptureDetail.route)
                },
                initialLat = lat,
                initialLng = lng
            )
        }

        /*──────── Onboarding 1 ────────*/
        composable(Screen.OnboardingOne.route) {
            IBichosWelcomeScreen(
                onStartClick = {
                    navController.navigate(Screen.OnboardingTwo.route)
                }
            )
        }

        /*──────── Onboarding 2 ────────*/
        composable(Screen.OnboardingTwo.route) {
            IBichosWelcomeTwoScreen(
                onStartClick = {
                    onboardingPrefs.setOnboardingSeen()
                    authViewModel.checkProfileCompletion { isComplete ->
                        val nextRoute = if (isComplete) Screen.Main.route else Screen.CompleteProfile.route
                        navController.navigate(nextRoute) {
                            popUpTo(Screen.OnboardingTwo.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.CompleteProfile.route) {
            CompleteProfileScreen(
                onSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.CompleteProfile.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }
    }
}

/*──────────────── MainScreenWithBottomNav ────────────────*/
@Composable
fun MainScreenWithBottomNav(
    onNavigateToDetail: (CaptureItem) -> Unit,
    onNavigateToMap: () -> Unit,
    onLogout: () -> Unit
) {

    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

                        onClick = {
                            if (!selected) {
                                bottomNavController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        },

                        icon = {
                            Icon(
                                if (selected) item.selectedIcon
                                else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },

                        label = { Text(item.label) },

                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IBichosGreen,
                            selectedTextColor = IBichosGreen,
                            indicatorColor = IBichosGreenDim,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

    ) { paddingValues ->

        NavHost(
            navController = bottomNavController,
            startDestination = "camera",
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {

            composable("camera") {
                CameraScreen()
            }

            composable("catalog") {
                val catalogViewModel: CatalogViewModel = viewModel()

                CatalogScreen(
                    viewModel = catalogViewModel,
                    onNavigateToMap = onNavigateToMap,
                    onNavigateToDetail = { capture ->
                        onNavigateToDetail(capture)
                    }
                )
            }

            composable("profile") {
                ProfileScreen(onLogout = onLogout)
            }

            composable("ranking") {
                RankingScreen()
            }
        }
    }
}