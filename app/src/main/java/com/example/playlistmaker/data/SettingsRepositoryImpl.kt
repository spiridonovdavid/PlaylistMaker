package com.example.playlistmaker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.SettingsRepository

class SettingsRepositoryImpl(context: Context) : SettingsRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun switchTheme(isDarkTheme: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME, isDarkTheme).apply()
    }

    override fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }

    companion object {
        private const val PREFS_NAME = "THEME_PREFS"
        private const val KEY_DARK_THEME = "DARK_THEME"
    }
}
