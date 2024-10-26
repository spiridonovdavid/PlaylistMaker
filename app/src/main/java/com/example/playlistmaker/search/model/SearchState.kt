package com.example.playlistmaker.search.model

sealed class SearchState {
    data object Loading : SearchState()
    data class Success(val tracks: List<Track>) : SearchState()
    data class Error(val messageResId: Int) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
}

sealed class HistoryState {
    data class ShowHistory(val historyTracks: List<Track>) : HistoryState()
    object HideHistory : HistoryState()
}