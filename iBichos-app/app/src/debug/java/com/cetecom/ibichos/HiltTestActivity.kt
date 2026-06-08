package com.cetecom.ibichos

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity mínima usada en tests de UI con Hilt.
 * Permite montar composables con hiltViewModel() sin arrancar MainActivity.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
