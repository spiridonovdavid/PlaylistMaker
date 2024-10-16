package com.example.playlistmaker.data.mappers

import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.domain.models.Track

class TrackMapper {
    fun mapTrackDtoToDomain(trackDto: TrackDto): Track {
        return Track(
            trackName = trackDto.trackName ?: "Неизвестный трек",
            artistName = trackDto.artistName ?: "Неизвестный Артист",
            artworkUrl100 = trackDto.artworkUrl100 ?: "",
            trackTimeMillis = trackDto.trackTimeMillis ?: 0L,
            collectionName = trackDto.collectionName ?: "Неизвестный альбом",
            releaseDate = trackDto.releaseDate ?: "Неизвестная дата",
            primaryGenreName = trackDto.primaryGenreName ?: "Неизвестный жанр",
            country = trackDto.country ?: "Неизвестная страна",
            previewUrl = trackDto.previewUrl
        )
    }
}