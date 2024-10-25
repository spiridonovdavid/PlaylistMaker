package com.example.playlistmaker

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.creator.Creator

class App:Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        applySavedTheme()
    }

    private fun applySavedTheme() {
        val settingsInteractor = Creator.provideSettingsInteractor()
        val themeSettings = settingsInteractor.getThemeSettings()

        AppCompatDelegate.setDefaultNightMode(
            if (themeSettings.isDarkTheme) {
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