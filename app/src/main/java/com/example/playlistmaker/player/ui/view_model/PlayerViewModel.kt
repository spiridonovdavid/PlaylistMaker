package com.example.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.api.PlayerInteractor
import com.example.playlistmaker.player.model.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PlayerViewModel(private val playerInteractor: PlayerInteractor) : ViewModel() {

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> get() = _playerState

    private val _currentPosition = MutableLiveData<String>()
    val currentPosition: LiveData<String> get() = _currentPosition

    private var updateJob: Job? = null

    init {
        _playerState.value = PlayerState.Default
    }

    fun preparePlayer(url: String?) {
        url?.let {
            playerInteractor.preparePlayer(it, onReady = {
                _playerState.postValue(PlayerState.Prepared)
            }, onComplete = {
                stopPositionUpdates()
                _playerState.postValue(PlayerState.Prepared)
                _currentPosition.postValue(formatTime(0))
            })
        }
    }

    fun startPlayer() {
        playerInteractor.startPlayer()
        _playerState.value = PlayerState.Playing
        startPositionUpdates()
    }

    fun pausePlayer() {
        playerInteractor.pausePlayer()
        _playerState.value = PlayerState.Paused
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
                _currentPosition.postValue(formatTime(position))
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