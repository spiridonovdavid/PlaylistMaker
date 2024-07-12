package com.practicum.playlistmaker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Track(
    val trackName: String,
    val artistName: String,
    @SerializedName("trackTimeMillis") var trackTime: Int,
    val artworkUrl100: String
) : Parcelable