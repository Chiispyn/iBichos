package com.cetecom.ibichos.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val IBichosDarkColorScheme = darkColorScheme(
    primary            = IBichosGreen,
    onPrimary          = OnGreen,
    primaryContainer   = IBichosGreenDim,
    onPrimaryContainer = IBichosGreenDark,
    secondary          = IBichosTeal,
    onSecondary        = OnGreen,
    tertiary           = IBichosAmber,
    onTertiary         = OnGreen,
    error              = IBichosRed,
    onError            = OnDark,
    background         = DarkBackground,
    onBackground       = OnDark,
    surface            = DarkSurface,
    onSurface          = OnDark,
    surfaceVariant     = DarkSurfaceVariant,
    onSurfaceVariant   = OnDarkSecondary,
    outline            = DarkOutline,
    outlineVariant     = DarkSurfaceVariant,
)

private val IBichosLightColorScheme = lightColorScheme(
    primary            = IBichosGreen,
    onPrimary          = OnGreen,
    primaryContainer   = IBichosGreenDark,
    onPrimaryContainer = OnLight,
    secondary          = IBichosTeal,
    onSecondary        = OnGreen,
    tertiary           = IBichosAmber,
    onTertiary         = OnGreen,
    error              = IBichosRed,
    onError            = OnLight,
    background         = LightBackground,
    onBackground       = OnLight,
    surface            = LightSurface,
    onSurface          = OnLight,
    surfaceVariant     = LightSurfaceVariant,
    onSurfaceVariant   = OnLightSecondary,
    outline            = LightOutline,
    outlineVariant     = LightSurfaceVariant,
)

@Composable
fun IBichosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        //colorScheme = if (darkTheme) IBichosDarkColorScheme else IBichosLightColorScheme,
        colorScheme = IBichosLightColorScheme,
        typography  = IBichosTypography,
        content     = content
    )
}
