package com.example.musicplayer.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.repositories.SongsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SongsViewModel(private val repository: SongsRepository, private val playlist: Playlist?): ViewModel() {
    private val TAG: String = "SongsViewModel"

    lateinit var songLiveData: LiveData<List<Song>>

    init {
        if (playlist == null){
            songLiveData = getAllSongs()
            Log.d(TAG, "Updating songs")
            updateSongs()
        }
//        else{
//            val list = playlist.songs?.keys?.toList()
//            list?.let {
//
//                songLiveData = repository.songDao.getSongsByIds(list)
//            }
//            // TODO: show message to add songs if no songs in playlist
//        }
    }

    private fun updateSongs()
    {
        viewModelScope.async(Dispatchers.IO) {
            val songs = repository.songsFirebase.readSongsUsingCoroutines(FirebaseConsts.songsDatabaseRef)
            songs?.let {
                Log.d(TAG, "Update songs. Count: ${it.size}")
                repository.songDao.deleteAllSongs()
                repository.songDao.insertAllSongs(songs)
            }
        }
    }

    private fun getAllSongs(): LiveData<List<Song>> {
        return repository.songDao.getAllSongs()
    }
}

class SongsViewModelFactory(private val repository: SongsRepository, private val playlist: Playlist? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongsViewModel(repository, playlist) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}