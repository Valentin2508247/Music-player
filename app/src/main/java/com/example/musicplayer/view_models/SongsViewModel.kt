package com.example.musicplayer.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.repositories.SongsRepository
import io.reactivex.Maybe

class SongsViewModel(private val repository: SongsRepository): ViewModel() {
    private val TAG: String = "SongsViewModel"

    init {
        updateAllSongs()
    }

    fun updateAllSongs()
    {
        Log.d(TAG, "Updating songs from SongsViewModel")
        val songsUpdater = object: SongsFirebase.IUpdateSongs{
            override fun updateSongs(songs: MutableList<Song>) {
                Log.d(TAG, "Updating songs from SongsViewModel")
                repository.songDao.deleteAllSongs()
                repository.songDao.insertAllSongs(songs)
            }
        }
        repository.songsFirebase.readSongs(FirebaseConsts.songsRef, songsUpdater)
    }

    fun getAllSongs(): LiveData<List<Song>>{
        return repository.songDao.getAllSongs()
    }
}

class SongsViewModelFactory(private val repository: SongsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}