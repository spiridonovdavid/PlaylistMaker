package com.example.playlistmaker.presentation.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.tracks.TrackViewHolder

class TrackAdapter(
    private var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tracklist_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}