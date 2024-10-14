package com.example.playlistmaker.data.dto

data class TrackSearchResponse(
    val resultCount: Int,
    val results: ArrayList<TrackDto>
) : Response()