package com.example.playlistmaker.media.data.impl

import com.example.playlistmaker.media.data.converters.PlaylistDbConverter
import com.example.playlistmaker.media.db.AppDatabase
import com.example.playlistmaker.media.db.TrackInPlaylistEntity
import com.example.playlistmaker.media.db.dao.TrackInPlaylistDao
import com.example.playlistmaker.media.domain.api.PlaylistRepository
import com.example.playlistmaker.media.model.Playlist
import com.example.playlistmaker.search.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class PlaylistRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistConverter: PlaylistDbConverter,
    private val trackInPlaylistDao: TrackInPlaylistDao,
    private val gson: Gson
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist) {
        appDatabase.playlistDao().addPlaylist(playlistConverter.map(playlist))
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().updatePlaylist(playlistConverter.map(playlist))
    }

    override suspend fun getAllPlaylists(): Flow<List<Playlist>> = flow {
        val playlists = appDatabase.playlistDao().getAllPlaylists().map {
            playlistConverter.map(it)
        }
        emit(playlists)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        val playlistEntity = appDatabase.playlistDao().getPlaylistById(playlist.id!!)
        playlistEntity?.let { existingPlaylist ->
            val tracksList: MutableList<Long> = if (existingPlaylist.tracks.isNullOrEmpty()) {
                mutableListOf()
            } else {
                deserializeTracks(existingPlaylist.tracks).toMutableList()
            }

            if (!tracksList.contains(track.trackId.toLong())) {
                tracksList.add(track.trackId.toLong())

                val updatedTracks = serializeTracks(tracksList)

                val updatedPlaylist = existingPlaylist.copy(
                    tracks = updatedTracks,
                    numberOfTracks = tracksList.size.toLong()
                )
                appDatabase.playlistDao().updatePlaylist(updatedPlaylist)

                val trackEntity = mapTrackToTrackInPlaylistEntity(track)
                trackInPlaylistDao.insertTrack(trackEntity)
            }
        }
    }

    private fun serializeTracks(tracks: List<Long>): String {
        return gson.toJson(tracks)
    }

    private fun deserializeTracks(tracks: String): List<Long> {
        return gson.fromJson(tracks, object : TypeToken<List<Long>>() {}.type) ?: emptyList()
    }

    private fun mapTrackToTrackInPlaylistEntity(track: Track): TrackInPlaylistEntity {
        return TrackInPlaylistEntity(
            trackId = track.trackId,
            trackTimestamp = System.currentTimeMillis(),
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl
        )
    }
}