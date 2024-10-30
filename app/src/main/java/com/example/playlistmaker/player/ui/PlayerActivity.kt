package com.example.playlistmaker.player.ui

import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.App.Companion.TRACK_DT
import com.google.gson.Gson
import java.util.Locale
import androidx.core.view.isVisible
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.player.model.PlayerState
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.model.Track
import com.example.playlistmaker.utils.dpToPx
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private val viewModel by viewModel<PlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        val intentState = intent.extras
        val trackData = intentState?.getString(TRACK_DT)
        val track = Gson().fromJson(trackData, Track::class.java)
        setupTrackInfo(track)
        viewModel.preparePlayer(track.previewUrl)
        observePlayerState()
        observePlayerPosition()
        setupButtonListener()
    }

    private fun setupTrackInfo(track: Track) {
        val artworkUrl = track.artworkUrl100.replace("100x100bb.jpg", "512x512bb.jpg")
        Glide.with(this)
            .load(artworkUrl)
            .placeholder(R.drawable.placeholderplayer)
            .error(R.drawable.placeholderplayer)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(8.0F, this)))
            .into(binding.albumCover)

        with(binding) {
            songTitle.text = track.trackName
            artist.text = track.artistName
            durationValue.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
            yearValue.text = track.releaseDate.substring(0, 4)
            genreValue.text = track.primaryGenreName
            countryValue.text = track.country


            if (track.collectionName.isEmpty()) {
                albumLabel.isVisible = false
                binding.albumValue.isVisible = false
            } else {
                albumLabel.isVisible = true
                albumValue.isVisible = true
                albumValue.text = track.collectionName
            }
        }
    }

    private fun observePlayerState() {
        viewModel.playerState.observe(this) { state ->
            when (state) {
                is PlayerState.Playing -> binding.playButton.setImageResource(R.drawable.pausebutton)
                is PlayerState.Paused, is PlayerState.Prepared -> binding.playButton.setImageResource(R.drawable.playbutton)
                else -> {binding.playButton.setImageResource(R.drawable.playbutton)}
            }
        }
    }

    private fun observePlayerPosition() {
        viewModel.currentPosition.observe(this) { position ->
            binding.durationPlaying.text = position
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }

    private fun setupButtonListener() {
        binding.playButton.setOnClickListener {
            playbackControl()
        }
    }
    private fun playbackControl() {
        when (viewModel.playerState.value) {
            is PlayerState.Playing -> viewModel.pausePlayer()
            is PlayerState.Paused, PlayerState.Prepared -> viewModel.startPlayer()
            else -> Unit
        }
    }
}
