package com.example.playlistmaker.search.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.search.model.Track
import com.example.playlistmaker.utils.dpToPx
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val trackNameTextView: TextView = view.findViewById(R.id.track_name)
    private val artistNameTextView: TextView = view.findViewById(R.id.track_artist)
    private val trackTimeTextView: TextView = view.findViewById(R.id.track_duration)
    private val artworkImageView: ImageView = view.findViewById(R.id.track_photo)

    fun bind(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        trackTimeTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.placeholderplayer)
            .fitCenter()
            .transform(RoundedCorners(dpToPx(2.0F, itemView.context)))
            .into(artworkImageView)
    }
}
