package com.example.playlistmaker.data

import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson

class SearchHistoryRepositoryImpl(private val sharedPreferences: SharedPreferences) : SearchHistoryRepository {

    private val gson = Gson()

    override fun saveTrack(track: Track) {
        val history = getHistory().toMutableList().apply {
            remove(track)
            add(0, track)
            if (size > MAX_HISTORY_SIZE) removeLast()
        }
        saveHistory(history)
    }

    override fun getHistory(): List<Track> {
        return sharedPreferences.getString(SEARCH_HISTORY_KEY, null)?.let { json ->
            gson.fromJson(json, Array<Track>::class.java).toList()
        } ?: emptyList()
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    private fun saveHistory(history: List<Track>) {
        sharedPreferences.edit()
            .putString(SEARCH_HISTORY_KEY, gson.toJson(history))
            .apply()
    }

    companion object {
        private const val SEARCH_HISTORY_KEY = "SEARCH_HISTORY"
        private const val MAX_HISTORY_SIZE = 10
    }
}
