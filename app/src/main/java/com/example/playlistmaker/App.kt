package com.example.playlistmaker

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.data.SettingsRepositoryImpl

class App:Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        applySavedTheme()
    }

    private fun applySavedTheme() {
        val themeRepository = SettingsRepositoryImpl(this)
        val isDarkTheme = themeRepository.isDarkThemeEnabled()

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    companion object {
        const val TRACK_DATA = "TRACK"
        lateinit var instance: App
            private set
        fun getAppContext(): Context {
            return instance.applicationContext
        }
    }
}