package com.example.musicplayer.fragments

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
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
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.fragments.dummy.DummyContent
import com.example.musicplayer.repositories.SongsRepository
import com.example.musicplayer.view_models.PlaylistsViewModel
import com.example.musicplayer.view_models.PlaylistsViewModelFactory
import com.example.musicplayer.view_models.SongsViewModel
import com.example.musicplayer.view_models.SongsViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * A fragment representing a list of Items.
 */
class SongListFragment : Fragment() {
    private val TAG = "SongListFragment"

    private var columnCount = 1
    private var playlist: Playlist? = null

    private lateinit var viewModel: SongsViewModel
    private lateinit var mDatabase: AppDatabase
    private lateinit var repository: SongsRepository
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var songsAdapter: MySongListRecyclerViewAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            playlist = it.getSerializable(ARG_PLAYLIST) as Playlist
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song_list_list, container, false)


        val listItemClickListener = object :MySongListRecyclerViewAdapter.OnSongListItemClickListener{
            override fun onSongListItemClicked(song: Song) {
                Log.d(TAG, "$song")
//                val mediaPlayer = MediaPlayer().apply {
//                    setAudioAttributes(
//                            AudioAttributes.Builder()
//                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                                    .build()
//                    )
//                    setDataSource(requireContext())
//                    prepare()
//                    start()
//                }
            }
        }


        songsAdapter = MySongListRecyclerViewAdapter(requireContext(), listOf(Song()), listItemClickListener)
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
        const val ARG_PLAYLIST = "playlist"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, playlist: Playlist) =
            SongListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putSerializable(ARG_PLAYLIST, playlist)
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = requireContext()

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(context)
        repository = SongsRepository(mDatabase.songDao(), SongsFirebase(firebaseDatabase, firebaseStorage))
        viewModel = ViewModelProvider(this, SongsViewModelFactory(repository, playlist)).get(SongsViewModel::class.java)
        viewModel.songLiveData.observe(viewLifecycleOwner){
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