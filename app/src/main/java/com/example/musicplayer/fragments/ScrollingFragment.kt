package com.example.musicplayer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.R
import com.example.musicplayer.repositories.ScrollingRepository
import com.example.musicplayer.view_models.ScrollingViewModel
import com.example.musicplayer.view_models.ScrollingViewModelFactory
import com.example.musicplayer.view_models.SongsViewModel
import com.example.musicplayer.view_models.SongsViewModelFactory

class ScrollingFragment : Fragment() {
    private val TAG = "ScrollingFragment"

    private lateinit var viewModel: ScrollingViewModel
    private lateinit var repository: ScrollingRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scrolling, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        repository = ScrollingRepository()
        viewModel = ViewModelProvider(this, ScrollingViewModelFactory(repository)).get(ScrollingViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }


}