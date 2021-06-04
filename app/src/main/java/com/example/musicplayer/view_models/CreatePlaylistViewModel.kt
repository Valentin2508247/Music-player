package com.example.musicplayer.view_models

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.repositories.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class CreatePlaylistViewModel(private val repository: PlaylistRepository): ViewModel() {
    private val TAG: String = "PlaylistsViewModel"

    val imagePath = MutableLiveData<String>()
    val isFileSelected = MutableLiveData<Boolean>()

    fun uploadPlaylist(playlist: Playlist, isShared: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            playlist.imagePath = imagePath.value
            repository.playlistsFirebase.uploadPlaylist(playlist, isShared)
        }


//        viewModelScope.launch {
//            playlist.imagePath = imagePath.value
//            repository.playlistsFirebase.uploadPlaylist(playlist)
//        }
    }
}

class CreatePlaylistViewModelFactory(private val repository: PlaylistRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreatePlaylistViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}