package com.example.musicplayer.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.repositories.SongsRepository

class NowPlayingViewModel(private val repository: SongsRepository, private val playlist: Playlist?): ViewModel() {
    private val TAG: String = "NowPlayingViewModel"

    var songsLiveData: LiveData<List<Song>> = getAllSongs()

    private fun getAllSongs(): LiveData<List<Song>> {
        return repository.songDao.getAllSongs()
    }
}

class NowPlayingViewModelFactory(private val repository: SongsRepository, private val playlist: Playlist? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NowPlayingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NowPlayingViewModel(repository, playlist) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}