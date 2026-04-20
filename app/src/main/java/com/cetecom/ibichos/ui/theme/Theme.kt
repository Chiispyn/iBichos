package com.cetecom.ibichos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val IBichosDarkColorScheme = darkColorScheme(
    primary          = IBichosGreen,
    onPrimary        = OnGreen,
    primaryContainer = IBichosGreenDim,
    onPrimaryContainer = IBichosGreenDark,

    secondary        = IBichosTeal,
    onSecondary      = OnGreen,

    tertiary         = IBichosAmber,
    onTertiary       = OnGreen,

    error            = IBichosRed,
    onError          = OnDark,

    background       = DarkBackground,
    onBackground     = OnDark,

    surface          = DarkSurface,
    onSurface        = OnDark,
    surfaceVariant   = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSecondary,

    outline          = DarkOutline,
    outlineVariant   = DarkSurfaceVariant,
)

private val IBichosLightColorScheme = lightColorScheme(
    primary          = IBichosGreen,
    onPrimary        = OnGreen,
    primaryContainer = IBichosGreenDark,
    onPrimaryContainer = OnLight,

    secondary        = IBichosTeal,
    onSecondary      = OnGreen,

    tertiary         = IBichosAmber,
    onTertiary       = OnGreen,

    error            = IBichosRed,
    onError          = OnLight,

    background       = LightBackground,
    onBackground     = OnLight,

    surface          = LightSurface,
    onSurface        = OnLight,
    surfaceVariant   = LightSurfaceVariant,
    onSurfaceVariant = OnLightSecondary,

    outline          = LightOutline,
    outlineVariant   = LightSurfaceVariant,
)

/**
 * Tema principal de iBichos — soporta light mode y dark mode,
 * paleta verde naturaleza + acentos de peligro.
 */
@Composable
fun IBichosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        IBichosDarkColorScheme
    } else {
        IBichosLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = IBichosTypography,
        content     = content
    )
}
