package com.example.playlistmaker.search.domain.impl

import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.api.TracksRepository
import com.example.playlistmaker.search.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    override fun searchTracks(query: String): Flow<List<Track>> {
        return repository.searchTracks(query)
            .map { tracks ->
                if (tracks.isNotEmpty()) tracks else emptyList()
            }
            .catch { e ->
                throw e
            }
    }
}