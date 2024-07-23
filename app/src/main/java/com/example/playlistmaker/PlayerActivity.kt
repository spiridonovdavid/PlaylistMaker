package com.example.playlistmaker

import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
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

        val intentState  = getIntent().getExtras();
        val trackData = intentState?.getString(TRACK_DATA)
        val track = Gson().fromJson(trackData, Track::class.java)

        backButton.setOnClickListener{
            finish()
        }

        Glide.with(this)
            .load(track.artworkUrl100?.replaceAfterLast('/',"512x512bb.jpg"))
            .placeholder(R.drawable.placeholderplayer)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(8f, this)))
            .into(albumImage)

        trackName.text = track.trackName
        artistName.text = track.artistName

        if(track.collectionName?.isEmpty() == true){
            albumLabel.isVisible = false
            albumValue.isVisible = false
        }else{
            albumLabel.isVisible = true
            albumValue.isVisible = true
            albumValue.text = track.collectionName
        }

        durationPlaying.text = "0:00"
        trackDuration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTime)
        yearValue.text = track.releaseDate.substring(0,4)
        genreValue.text = track.primaryGenreName
        trackCountry.text = track.country

    }
}