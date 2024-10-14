package com.example.playlistmaker.data

import com.example.playlistmaker.data.dto.TrackSearchRequest
import com.example.playlistmaker.data.dto.TrackSearchResponse
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.data.mappers.TrackMapper
import java.io.IOException

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {
    private val mapper = TrackMapper()

    override fun searchTracks(query: String): List<Track> {
        val response = networkClient.doRequest(TrackSearchRequest(query))

        return when (response.resultCode) {
            200 -> (response as TrackSearchResponse).results.map(mapper::mapTrackDtoToDomain)
            500 -> throw IOException("Ошибка сети")
            else -> emptyList()
        }
    }
}
