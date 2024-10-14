package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.PlayerRepositoryImpl
import com.example.playlistmaker.data.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.SettingsRepositoryImpl
import com.example.playlistmaker.data.TracksRepositoryImpl
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.domain.api.PlayerInteractor
import com.example.playlistmaker.domain.api.PlayerRepository
import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.domain.api.SettingsInteractor
import com.example.playlistmaker.domain.api.SettingsRepository
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.impl.PlayerInteractorImpl
import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.impl.TracksInteractorImpl

object Creator {

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    fun provideSettingsInteractor(): SettingsInteractor {
        return SettingsInteractorImpl(getThemeRepository())
    }

    fun provideSearchHistoryInteractor(): SearchHistoryInteractor {
        val sharedPreferences = App.getAppContext().getSharedPreferences("SEARCH_PREFS", Context.MODE_PRIVATE)
        return SearchHistoryInteractorImpl(SearchHistoryRepositoryImpl(sharedPreferences))
    }
    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(getPlayerRepository())
    }

    // TracksRepository использует сетевой клиент, инициализированный с помощью Retrofit
    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    private fun getThemeRepository(): SettingsRepository {
        return SettingsRepositoryImpl(App.getAppContext())
    }

    private fun getPlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }


}