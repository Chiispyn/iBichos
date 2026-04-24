package com.cetecom.ibichos.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cetecom.ibichos.R
import com.cetecom.ibichos.ui.theme.IBichosGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Animación Alpha y Escala para el logo central
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.5f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
    )

    // Lanzador del hilo de tiempo (3 segundos)
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000L)
        onSplashFinished()
    }

    // Usamos el mismo diseño en el fondo para que la transición sea pura
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IBichosGreen.copy(alpha = 0.3f), MaterialTheme.colorScheme.background)
                )
            )
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_oficial),
            contentDescription = "Logo Oficial iBichos",
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp)
                .scale(logoScale)
                .alpha(logoAlpha)
        )
    }
}

@Composable
fun AnimatedLeaf(modifier: Modifier = Modifier, baseRotation: Float) {
    val infiniteTransition = rememberInfiniteTransition()

    // Animación sinfín simulando flotar (arriba a abajo)
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Image(
        painter = painterResource(id = R.drawable.hoja_animada),
        contentDescription = "Hoja decorativa flotando",
        modifier = modifier
            .offset(y = offsetY.dp)
            .size(80.dp)
            .rotate(baseRotation)
    )
}
