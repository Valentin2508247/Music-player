package com.example.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.PlaylistsFirebase
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.fragments.strategy.SongsSource
import com.example.musicplayer.repositories.PlaylistRepository
import com.example.musicplayer.repositories.SongsRepository
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
    private var playlistId: String? = null
    private var songsSource: SongsSource = SongsSource.AllSongs()
    private lateinit var listener: SongItemClickListener

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
            val songsSourceCode = it.getInt(ARG_SONGS_SOURCE)
            songsSource = SongsSource.getSongSource(songsSourceCode)
            playlistId = it.getString(ARG_PLAYLIST)
            //playlist = it.getSerializable(ARG_PLAYLIST) as Playlist

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
                listener.songItemClick(song)
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

            override fun onSongLiked(song: Song, likes: HashMap<String, Boolean>) {
                Log.d(TAG, "Song liked: ${song.id}")
                viewModel.saveToLocal(likes)
                viewModel.uploadLikes(likes)
            }

            override fun playSongs(pos: Int, data: List<Song>) {
                listener.playSong(pos, data)
                // TODO("Not yet implemented")
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
        const val ARG_SONGS_SOURCE = "songs-source"
        const val ARG_PLAYLIST = "playlist"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, songsSource: Int, playlistId: String? = null) =
            SongListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putInt(ARG_SONGS_SOURCE, songsSource)
                    playlistId?.let {
                        putString(ARG_PLAYLIST, playlistId)
                    }
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = requireContext()

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(context)
        repository = SongsRepository(mDatabase.songDao(), mDatabase.likesDao(), SongsFirebase(firebaseDatabase, firebaseStorage))
        val playlistRepository = PlaylistRepository(mDatabase.playlistDao(), PlaylistsFirebase(firebaseDatabase, firebaseStorage))
        viewModel = ViewModelProvider(this, SongsViewModelFactory(repository, playlistId, songsSource, playlistRepository)).get(SongsViewModel::class.java)

        viewModel.songLiveData.observe(viewLifecycleOwner){
            Log.d(TAG, "LiveData observe")
            //val likes = viewModel.likesLiveData.value
            songsAdapter.setData(it)
        }

        viewModel.likesLiveData.observe(viewLifecycleOwner){
            Log.d(TAG, "Likes LiveData observe")
            songsAdapter.setLikes(it)
        }
        // TODO: Use the ViewModel


    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    interface SongItemClickListener{
        fun songItemClick(song: Song)
        fun playSong(pos: Int, songs: List<Song>)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as SongItemClickListener
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }
    }
}