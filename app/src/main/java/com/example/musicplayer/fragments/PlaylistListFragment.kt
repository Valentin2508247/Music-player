package com.example.musicplayer.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.R
import com.example.musicplayer.activities.PlaylistActivity
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.PlaylistsFirebase
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.fragments.dummy.DummyContent
import com.example.musicplayer.repositories.PlaylistRepository
import com.example.musicplayer.repositories.SongsRepository
import com.example.musicplayer.view_models.PlaylistsViewModel
import com.example.musicplayer.view_models.PlaylistsViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * A fragment representing a list of Items.
 */
class PlaylistListFragment : Fragment() {
    private val TAG = "PlaylistListFragment"

    private var columnCount = 1
    private lateinit var playlstsAdapter: MyPlaylistRecyclerViewAdapter

    private lateinit var viewModel: PlaylistsViewModel
    private lateinit var repository: PlaylistRepository
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var mDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get args from bundle
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = requireContext()

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(context)
        repository = PlaylistRepository(mDatabase.playlistDao(), PlaylistsFirebase(firebaseDatabase, firebaseStorage))

        viewModel = ViewModelProvider(this, PlaylistsViewModelFactory(repository)).get(PlaylistsViewModel::class.java)
        viewModel.playlistsLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "LiveData observe")
            it?.let {
                playlstsAdapter.setData(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_list, container, false)


        val listItemClickListener = object : MyPlaylistRecyclerViewAdapter.OnPlaylistListItemClickListener{
            override fun onPlaylistItemClicked(playlist: Playlist) {
                Log.d(TAG, "$playlist")
                Toast.makeText(context, playlist.songs?.keys.toString(), Toast.LENGTH_LONG).show()
                val intent = Intent(context, PlaylistActivity::class.java)
                intent.putExtra(getString(R.string.intent_playlist_key), playlist)
                startActivity(intent)
            }
        }

        playlstsAdapter = MyPlaylistRecyclerViewAdapter(requireContext(), emptyList<Playlist>(), listItemClickListener)
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = playlstsAdapter
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
            PlaylistListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }
}