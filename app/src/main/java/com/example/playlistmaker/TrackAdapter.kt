package com.example.playlistmaker

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(private var trackList: MutableList<Track>) : RecyclerView.Adapter<TrackViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tracklist_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position])

        holder.itemView.setOnClickListener {
            if(clickDebounce()) {
                saveHistorySearch(
                    (holder.itemView.context.applicationContext as App),
                    trackList[position]
                )

                val initIntent = Intent(holder.itemView.context, PlayerActivity::class.java)
                initIntent.putExtra(TRACK_DATA, trackList[position])
                holder.itemView.context.startActivity(initIntent)

                if (trackList[position].isHistory) {
                    trackList = getHistorySearch(holder.itemView.context.applicationContext as App)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount() = trackList.size

    private var isClickAllowed = true

    private val handler = Handler(Looper.getMainLooper())

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }
    private companion object {
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }

}