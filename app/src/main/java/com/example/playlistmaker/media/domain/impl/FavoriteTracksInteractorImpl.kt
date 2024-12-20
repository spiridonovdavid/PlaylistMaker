package com.example.playlistmaker.media.domain.impl

import com.example.playlistmaker.media.domain.api.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.api.FavoriteTracksRepository
import com.example.playlistmaker.search.model.Track
import kotlinx.coroutines.flow.Flow

class FavoriteTracksInteractorImpl(
    private val favoriteTracksRepository: FavoriteTracksRepository
): FavoriteTracksInteractor {

    override suspend fun addFavoriteTrack(track: Track) {
        favoriteTracksRepository.addFavoriteTrack(track)
    }

    override suspend fun deleteFavoriteTrack(track: Track) {
        favoriteTracksRepository.deleteFavoriteTrack(track)
    }

    override fun favoriteTracks(): Flow<List<Track>> {
        return favoriteTracksRepository.favoriteTracks()
    }

    override suspend fun isTrackFavorite(trackId: Int): Boolean {
        return favoriteTracksRepository.isTrackFavorite(trackId)
    }
}