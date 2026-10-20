package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("harmonix_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "key_has_completed_onboarding"
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun saveUserName(name: String) {
        val trimmed = name.trim()
        prefs.edit()
            .putString(KEY_USER_NAME, trimmed)
            .putBoolean(KEY_HAS_COMPLETED_ONBOARDING, true)
            .apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false) && !getUserName().isNullOrBlank()
    }
}
