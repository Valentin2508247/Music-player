package com.example.musicplayer.view_models

import com.example.musicplayer.repositories.SongsRepository
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.repositories.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class UploadSongViewModel(private val repository: SongsRepository): ViewModel() {
    private val TAG: String = "UploadSongViewModel"

    val imagePath = MutableLiveData<String>()
    val isImageFileSelected = MutableLiveData<Boolean>()

    val songPath = MutableLiveData<String>()
    val isSongFileSelected = MutableLiveData<Boolean>()

    fun uploadSong(song: Song) {
        GlobalScope.launch(Dispatchers.IO) {
            song.imagePath = imagePath.value
            song.songPath = songPath.value
            repository.songsFirebase.uploadSong(song)
        }


//        viewModelScope.launch {
//            playlist.imagePath = imagePath.value
//            repository.playlistsFirebase.uploadPlaylist(playlist)
//        }
    }
}

class UploadSongViewModelFactory(private val repository: SongsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadSongViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UploadSongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}