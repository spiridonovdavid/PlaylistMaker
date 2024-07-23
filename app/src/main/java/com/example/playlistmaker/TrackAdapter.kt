package com.example.playlistmaker

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class TrackAdapter(private var trackList: MutableList<Track>) : RecyclerView.Adapter<TrackViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tracklist_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position])

        holder.itemView.setOnClickListener {

            saveHistorySearch(
                (holder.itemView.context.applicationContext as App),
                trackList[position]
            )

            val json = Gson().toJson(trackList[position])
            val initIntent = Intent(holder.itemView.context, PlayerActivity::class.java)
            initIntent.putExtra(TRACK_DATA, json)
            holder.itemView.context.startActivity(initIntent)

            if (trackList[position].isHistory) {
                trackList = getHistorySearch(holder.itemView.context.applicationContext as App)
                notifyDataSetChanged()
            }

        }
    }

    override fun getItemCount() = trackList.size


}