package com.example.playlistmaker.search.domain.impl

import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.api.TracksRepository
import java.io.IOException

class TracksInteractorImpl(private val repository: TracksRepository) :
    TracksInteractor {
    override fun searchTracks(query: String, consumer: TracksInteractor.TracksConsumer, onError: (Throwable) -> Unit) {
        val thread = Thread {
            try {
                val foundTracks = repository.searchTracks(query)
                consumer.consume(foundTracks)
            } catch (e: IOException) {
                onError(e)
            } catch (e: Exception) {
                consumer.consume(emptyList())
            }
        }
        thread.start()
    }
}
