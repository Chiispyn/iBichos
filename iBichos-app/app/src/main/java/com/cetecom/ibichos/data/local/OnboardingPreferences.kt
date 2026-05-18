package com.cetecom.ibichos.data.local

import android.content.Context

class OnboardingPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("ibichos_prefs", Context.MODE_PRIVATE)

    fun isOnboardingSeen(): Boolean = prefs.getBoolean("onboarding_seen", false)

    fun setOnboardingSeen() {
        prefs.edit().putBoolean("onboarding_seen", true).apply()
    }
}
