package com.example.playlistmaker.domain.impl

import android.util.Log
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.api.TracksRepository
import java.io.IOException

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {
    override fun searchTracks(expression: String, consumer: TracksInteractor.TracksConsumer, onError: (Throwable) -> Unit) {
        val thread = Thread {
            try {
                val foundTracks = repository.searchTracks(expression)
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
