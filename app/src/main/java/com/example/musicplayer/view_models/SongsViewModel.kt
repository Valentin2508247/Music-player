package com.example.musicplayer.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.repositories.SongsRepository

class SongsViewModel(private val repository: SongsRepository): ViewModel() {
    private val TAG: String = "SongsViewModel"


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