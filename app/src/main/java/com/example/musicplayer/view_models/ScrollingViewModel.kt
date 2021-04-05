package com.example.musicplayer.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.repositories.ScrollingRepository

class ScrollingViewModel(private val repository: ScrollingRepository): ViewModel() {
    private val TAG = "ScrollingViewModel"


}

class ScrollingViewModelFactory(private val repository: ScrollingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScrollingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScrollingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}