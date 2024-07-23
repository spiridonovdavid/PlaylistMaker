package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

const val SHARED_PREFS = "SHARED_PREFS"
const val THEME_MODE = "THEME_SWITCHER"
const val HISTORY_SEARCH = "HISTORY_SEARCH"
const val TRACK_DATA = "TRACK_DATA"

class App : Application() {

    var darkTheme = false
    var sharedPrefs: SharedPreferences? = null

    override fun onCreate() {
        super.onCreate()

        sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        darkTheme = sharedPrefs?.getBoolean(THEME_MODE, false) ?: false // Включаем тему из sharedPrefs при старте App
        switchTheme(darkTheme)

    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

}