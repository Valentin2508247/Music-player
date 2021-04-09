package com.example.musicplayer.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.R
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.fragments.dummy.DummyContent
import com.example.musicplayer.repositories.SongsRepository
import com.example.musicplayer.view_models.PlaylistsViewModel
import com.example.musicplayer.view_models.PlaylistsViewModelFactory
import com.example.musicplayer.view_models.SongsViewModel
import com.example.musicplayer.view_models.SongsViewModelFactory
import com.google.firebase.database.FirebaseDatabase

/**
 * A fragment representing a list of Items.
 */
class SongListFragment : Fragment() {
    private val TAG = "SongListFragment"

    private var columnCount = 1

    private lateinit var viewModel: SongsViewModel
    private lateinit var mDatabase: AppDatabase
    private lateinit var repository: SongsRepository
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var songsAdapter: MySongListRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song_list_list, container, false)

        songsAdapter = MySongListRecyclerViewAdapter(listOf(Song()))
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = songsAdapter
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            SongListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = requireContext()

        firebaseDatabase = FirebaseDatabase.getInstance()
        mDatabase = AppDatabase.getDatabase(context)
        repository = SongsRepository(mDatabase.songDao(), SongsFirebase(firebaseDatabase))
        viewModel = ViewModelProvider(this, SongsViewModelFactory(repository)).get(SongsViewModel::class.java)
        viewModel.getAllSongs().observe(viewLifecycleOwner){
            Log.d(TAG, "LiveData observe")
            songsAdapter.setData(it)
        }
        // TODO: Use the ViewModel
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }
}