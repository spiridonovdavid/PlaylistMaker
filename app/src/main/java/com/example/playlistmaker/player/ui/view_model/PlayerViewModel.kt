package com.example.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.api.FavoriteTracksInteractor
import com.example.playlistmaker.player.domain.api.PlayerInteractor
import com.example.playlistmaker.player.model.PlayerState
import com.example.playlistmaker.search.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {
    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> get() = _playerState

    private val _favoriteState = MutableLiveData<Boolean>()
    val favoriteState: LiveData<Boolean> get() = _favoriteState

    private var updateJob: Job? = null

    init {
        _playerState.value = PlayerState.Default
    }

    fun onFavoriteClicked(track: Track) {
        viewModelScope.launch {
            val isCurrentlyFavorite = _favoriteState.value ?: false
            if (isCurrentlyFavorite) {
                favoriteTracksInteractor.deleteFavoriteTrack(track)
                _favoriteState.postValue(false)
            } else {
                favoriteTracksInteractor.addFavoriteTrack(track)
                _favoriteState.postValue(true)
            }
        }
    }

    fun loadTrack(track: Track) {
        viewModelScope.launch {
            val isTrackFavorite = favoriteTracksInteractor.isTrackFavorite(track.trackId)
            _favoriteState.postValue(isTrackFavorite)
        }
    }

    fun preparePlayer(url: String?) {
        url?.let {
            playerInteractor.preparePlayer(it, onReady = {
                _playerState.postValue(PlayerState.Prepared)
            }, onComplete = {
                stopPositionUpdates()
                _playerState.postValue(PlayerState.Prepared)
            })
        }
    }

    fun startPlayer() {
        playerInteractor.startPlayer()
        _playerState.value = PlayerState.Playing(formatTime(playerInteractor.getCurrentPosition()))
        startPositionUpdates()
    }

    fun pausePlayer() {
        playerInteractor.pausePlayer()
        _playerState.value = PlayerState.Paused(formatTime(playerInteractor.getCurrentPosition()))
        stopPositionUpdates()
    }

    fun releasePlayer() {
        playerInteractor.releasePlayer()
        stopPositionUpdates()
    }

    private fun startPositionUpdates() {
        stopPositionUpdates() // Остановка любых предыдущих задач
        updateJob = viewModelScope.launch {
            while (true) {
                val position = playerInteractor.getCurrentPosition()
                _playerState.postValue(PlayerState.Playing(formatTime(position)))
                delay(UPDATE_DELAY)
            }
        }
    }

    private fun stopPositionUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    private fun formatTime(timeMillis: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(timeMillis)
    }

    companion object {
        private const val UPDATE_DELAY = 300L
    }
}