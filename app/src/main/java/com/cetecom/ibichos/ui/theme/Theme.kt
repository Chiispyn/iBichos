package com.cetecom.ibichos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

/**
 * Tema principal de iBichos — siempre dark mode,
 * paleta verde naturaleza + acentos de peligro.
 */
@Composable
fun IBichosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IBichosDarkColorScheme,
        typography  = IBichosTypography,
        content     = content
    )
}
