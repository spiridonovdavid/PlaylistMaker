package com.example.playlistmaker.search.data

import android.content.SharedPreferences
import com.example.playlistmaker.search.domain.api.SearchHistoryRepository
import com.example.playlistmaker.search.model.Track
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class SearchHistoryRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    SearchHistoryRepository {

    private val gson = Gson()

    override suspend fun saveTrack(track: Track) {
        val history = getHistoryInternal().toMutableList()
        history.removeIf { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeLast()
        }
        saveHistory(history)
    }

    override fun getHistory(): Flow<List<Track>> {
        return flow {
            val history = getHistoryInternal()
            emit(history)
        }
    }

    override suspend fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }

    private fun getHistoryInternal(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return emptyList()
        return gson.fromJson(json, Array<Track>::class.java).toList()
    }

    companion object {
        private const val SEARCH_HISTORY_KEY = "SEARCH_HISTORY"
        private const val MAX_HISTORY_SIZE = 10
    }
}
