package com.example.musicplayer.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.repositories.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async


class PlaylistsViewModel(private val repository: PlaylistRepository): ViewModel() {
    private val TAG: String = "PlaylistsViewModel"

    val playlistsLiveData: LiveData<List<Playlist>> = getAllPlaylists()

    init{
        //updatePlaylists()
    }

    private fun getAllPlaylists(): LiveData<List<Playlist>> {
        return repository.playlistDao.getAllPlaylists()
    }

    fun updatePlaylists(){
        viewModelScope.async(Dispatchers.IO) {
            val playlists = repository.playlistsFirebase.readPlaylistsUsingCoroutines(FirebaseConsts.playlistsDatabaseRef)
            playlists?.let{
                Log.d(TAG, "Deleting all playlists")
                repository.playlistDao.deleteAllPlaylists()
                Log.d(TAG, "Inserting playlists")
                repository.playlistDao.insertAllPlaylists(playlists)
            }
        }
    }
}

class PlaylistsViewModelFactory(private val repository: PlaylistRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//class MainViewModel(private val database: AppDatabase) : ViewModel(){
//    private val TAG: String = "MainViewModel"
//    val allCodes: LiveData<List<Code>> = database.codeDao().getAllCodes()
//}

//class MainViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return MainViewModel(database) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}