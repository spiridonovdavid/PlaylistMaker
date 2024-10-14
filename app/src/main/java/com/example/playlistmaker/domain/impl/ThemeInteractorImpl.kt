package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.data.SettingsRepositoryImpl
import com.example.playlistmaker.domain.api.ThemeInteractor
import com.example.playlistmaker.domain.api.SettingsRepository

class ThemeInteractorImpl(private val settingsRepository: SettingsRepository) : ThemeInteractor {

    override fun isDarkThemeEnabled(): Boolean {
        return settingsRepository.isDarkThemeEnabled()
    }
}
