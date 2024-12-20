package com.example.playlistmaker.search.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track (
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String?,
    val trackTimestamp: Long = System.currentTimeMillis(),
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Track) return false
        return trackId == other.trackId
    }

    override fun hashCode(): Int {
        return trackId.hashCode()
    }
}