package com.example.musicplayer.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.repositories.PlaylistRepository

class PlaylistsViewModel(private val repository: PlaylistRepository): ViewModel() {
    private val TAG: String = "PlaylistsViewModel"


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