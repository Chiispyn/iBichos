package com.cetecom.ibichos.data.local

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT, DARK
}

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        runCatching {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        }.getOrDefault(ThemeMode.DARK)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        prefs.edit { putString("theme_mode", mode.name) }
        _themeMode.value = mode
    }
}
