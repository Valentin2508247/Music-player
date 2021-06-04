package com.example.musicplayer.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.repositories.SongsRepository


class MainActivityViewModel(val songsRepository: SongsRepository): ViewModel() {
    private val TAG: String = "MainActivityViewModel"

    val playlist = MutableLiveData<Playlist>()
    val songs = MutableLiveData<List<Song>>()
}

class MainActivityViewModelFactory(val songsRepository: SongsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(songsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}