package com.example.musicplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.PlaylistsFirebase
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.fragments.AddSongsToPlaylistRecyclerViewAdapter
import com.example.musicplayer.repositories.PlaylistRepository
import com.example.musicplayer.repositories.SongsRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddSongsToPlaylistActivity : AppCompatActivity(),
    AddSongsToPlaylistRecyclerViewAdapter.OnSongListItemClickListener {
    private var TAG = "AddSongsToPlaylistActivity2"

    private lateinit var playlistId: String
    private lateinit var playlist: Playlist
    private lateinit var songs: List<Song>
    private lateinit var rvAdapter: AddSongsToPlaylistRecyclerViewAdapter

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var mDatabase: AppDatabase
    private lateinit var songsRepository: SongsRepository
    private lateinit var playlistsRepository: PlaylistRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_songs_to_playlist)

        playlistId = intent.getStringExtra("playlist")

        initDependencies()
        initViews()

        findViewById<Button>(R.id.button_ok).setOnClickListener() {
            savePlaylistToDb()
            finish()
        }
    }

    private fun initDependencies() {
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(this)
        songsRepository = SongsRepository(mDatabase.songDao(), mDatabase.likesDao(), SongsFirebase(firebaseDatabase, firebaseStorage))
        playlistsRepository = PlaylistRepository(mDatabase.playlistDao(), PlaylistsFirebase(firebaseDatabase, firebaseStorage))
    }

    private fun initViews() {
        val rv = findViewById<RecyclerView>(R.id.rv_add_songs_to_playlist)

        songs = songsRepository.songDao.getSongs()
        playlist = playlistsRepository.playlistDao.getPlaylist(playlistId)
        val hashMap = HashMap<String, Boolean>()
        for ((k, v) in playlist.songs!!){
            hashMap[k] = v
        }
        rvAdapter = AddSongsToPlaylistRecyclerViewAdapter(this, songs, this, hashMap)



        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = rvAdapter
    }

    private fun savePlaylistToDb() {
        playlist.songs = rvAdapter.playlist
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                playlistsRepository.playlistDao.insertPlaylist(playlist)
                playlistsRepository.playlistsFirebase.updatePlaylist(playlist, playlistId)
            }
            catch (ex: Exception){
                Log.d(TAG, ex.message)
            }
        }
    }

    override fun onSongListItemClicked(song: Song) {
        //TODO("Not yet implemented")
    }

    override fun onSongLiked(song: Song, likes: HashMap<String, Boolean>) {
        //TODO("Not yet implemented")
    }

    override fun playSongs(pos: Int, data: List<Song>) {
        //TODO("Not yet implemented")
    }
}