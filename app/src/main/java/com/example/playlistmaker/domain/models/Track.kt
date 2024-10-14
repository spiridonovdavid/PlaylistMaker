package com.example.playlistmaker.domain.models

data class Track (
    val trackName: String?,
    val artistName: String?,
    val artworkUrl100: String?,
    val trackTimeMillis: Long?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
)