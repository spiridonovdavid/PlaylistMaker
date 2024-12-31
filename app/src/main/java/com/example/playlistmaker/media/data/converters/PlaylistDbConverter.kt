package com.example.playlistmaker.media.data.converters

import com.example.playlistmaker.media.db.PlaylistEntity
import com.example.playlistmaker.media.model.Playlist

class PlaylistDbConverter {

    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            0,
            playlist.title,
            playlist.description,
            playlist.imageUri,
            playlist.tracks,
            playlist.numberOfTracks
        )
    }

    fun map(playlist: PlaylistEntity): Playlist {
        return Playlist(
            playlist.playlistId,
            playlist.playlistTitle,
            playlist.playlistDescription,
            playlist.playlistCoverUri,
            playlist.tracks,
            playlist.numberOfTracks
        )
    }

}