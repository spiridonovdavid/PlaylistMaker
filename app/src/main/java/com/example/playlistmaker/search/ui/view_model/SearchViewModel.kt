package com.example.playlistmaker.search.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.model.Track
import com.example.playlistmaker.search.model.HistoryState
import com.example.playlistmaker.search.model.SearchState
import java.io.IOException

class SearchViewModel(
    private val tracksInteractor: TracksInteractor
) : ViewModel() {

    private val searchState = MutableLiveData<SearchState>()
    fun getSearchState(): LiveData<SearchState> = searchState

    private val historyState = MutableLiveData<HistoryState>()
    fun getHistoryState(): LiveData<HistoryState> = historyState

    fun performSearch(query: String) {
        if (query.isEmpty()) return

        searchState.value = SearchState.Loading

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>) {

                if (foundTracks.isNotEmpty()) {
                    searchState.postValue(SearchState.Success(foundTracks))
                } else {
                    searchState.postValue(SearchState.Error(R.string.error_not_found))
                }
            }
        }) { throwable ->

            val errorMessageResId = if (throwable is IOException) {
                R.string.error_internet
            } else {
                R.string.error_not_found
            }
            searchState.postValue(SearchState.Error(errorMessageResId))
        }
    }

    companion object {
        fun provideFactory(
            tracksInteractor: TracksInteractor
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SearchViewModel(tracksInteractor) as T
                }
                throw IllegalArgumentException("Неизвестный класс ViewModel")
            }
        }
    }
}

