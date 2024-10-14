package com.example.playlistmaker.ui.player

import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.App.Companion.TRACK_DATA
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.PlayerInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.utility.dpToPx
import com.google.gson.Gson
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var albumImage: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var buttonAdd: ImageView
    private lateinit var buttonPlay: ImageView
    private lateinit var buttonLike: ImageView
    private lateinit var durationPlaying: TextView
    private lateinit var trackDuration: TextView
    private lateinit var albumLabel: TextView
    private lateinit var albumValue: TextView
    private lateinit var yearValue: TextView
    private lateinit var genreValue: TextView
    private lateinit var trackCountry: TextView
    private val playerInteractor: PlayerInteractor = Creator.providePlayerInteractor()
    private var playerState = STATE_DEFAULT
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                val currentPosition = playerInteractor.getCurrentPosition()
                durationPlaying.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentPosition)
                handler.postDelayed(this, DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        backButton = findViewById(R.id.back)
        albumImage = findViewById(R.id.albumCover)
        trackName = findViewById(R.id.songTitle)
        artistName = findViewById(R.id.artist)
        buttonAdd = findViewById(R.id.addToPlaylistButton)
        buttonPlay = findViewById(R.id.playButton)
        buttonLike = findViewById(R.id.likeButton)
        durationPlaying = findViewById(R.id.durationPlaying)
        trackDuration = findViewById(R.id.durationValue)
        albumLabel = findViewById(R.id.albumLabel)
        albumValue = findViewById(R.id.albumValue)
        yearValue = findViewById(R.id.yearValue)
        genreValue = findViewById(R.id.genreValue)
        trackCountry = findViewById(R.id.countryValue)

        val track = Gson().fromJson(intent.extras?.getString(TRACK_DATA), Track::class.java)
        println(track)
        backButton.setOnClickListener{
            finish()
        }

        Glide.with(this)
            .load(track?.artworkUrl100?.replaceAfterLast('/',"512x512bb.jpg"))
            .placeholder(R.drawable.placeholderplayer)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(8f, this)))
            .into(albumImage)

        trackName.text = track?.trackName
        artistName.text = track?.artistName

        if(track?.collectionName?.isEmpty() == true){
            albumLabel.isVisible = false
            albumValue.isVisible = false
        }else{
            albumLabel.isVisible = true
            albumValue.isVisible = true
            albumValue.text = track?.collectionName
        }

        durationPlaying.text = "00:00"
        trackDuration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track?.trackTimeMillis)
        yearValue.text = track?.releaseDate?.substring(0,4)
        genreValue.text = track?.primaryGenreName
        trackCountry.text = track?.country

        if(track?.previewUrl == null){
            Toast.makeText(this, "@string/nopreivewurl", Toast.LENGTH_SHORT).show()
        }

        preparePlayer(track?.previewUrl)

        buttonPlay.setOnClickListener {
            playbackControl()
        }

    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerInteractor.releasePlayer()
    }

    private fun pausePlayer() {
        playerInteractor.pausePlayer()
        playerState = STATE_PAUSED
        buttonPlay.setImageResource(R.drawable.playbutton)
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        playerState = STATE_PLAYING
        buttonPlay.setImageResource(R.drawable.pausebutton)
        handler.post(updateTimeRunnable)
    }

    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    private fun preparePlayer(url: String?) {
        if (url != null) {
            playerInteractor.preparePlayer(url, onReady = {
                buttonPlay.isEnabled = true
                playerState = STATE_PREPARED
            }, onComplete = {
                playerState = STATE_PREPARED
                handler.removeCallbacks(updateTimeRunnable)
                durationPlaying.text = "00:00"
                buttonPlay.setImageResource(R.drawable.playbutton)
            })
        } else {
            buttonPlay.isEnabled = false
        }
    }

    private companion object {
        const val STATE_DEFAULT = 0
        const val STATE_PREPARED = 1
        const val STATE_PLAYING = 2
        const val STATE_PAUSED = 3
        const val DELAY = 300L
    }
}