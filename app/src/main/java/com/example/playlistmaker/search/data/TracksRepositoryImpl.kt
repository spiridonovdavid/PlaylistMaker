package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.data.dto.TracksRequest
import com.example.playlistmaker.search.data.dto.TracksResponse
import com.example.playlistmaker.search.domain.api.TracksRepository
import com.example.playlistmaker.search.model.Track
import java.io.IOException
import com.example.playlistmaker.search.mappers.TrackMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {
    private val trackMapper = TrackMapper()

    override fun searchTracks(expression: String): Flow<List<Track>> {
        return flow {
            val response = networkClient.doRequest(TracksRequest(expression))

            if (response.resultCode == 200) {
                emit((response as TracksResponse).results.map { trackDto ->
                    trackMapper.mapTrackDtoToDomain(trackDto)
                })
            } else if (response.resultCode == 500) {
                throw IOException("Network Error")
            } else {
                emit(emptyList())
            }
        }.catch { exception ->
            throw exception
        }
    }
}
