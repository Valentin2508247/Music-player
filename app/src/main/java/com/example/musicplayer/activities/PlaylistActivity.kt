package com.example.musicplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.fragments.SongListFragment

class PlaylistActivity : AppCompatActivity() {
    private val TAG = "PlaylistActivity2"

    private lateinit var playlist: Playlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        playlist = intent.getSerializableExtra(getString(R.string.intent_playlist_key)) as Playlist
        playlist?.let {
            findViewById<TextView>(R.id.playlist_name).text = it.name
        }

        val songsFragment: SongListFragment = SongListFragment.newInstance(1, playlist)
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.add(R.id.fragment_songs_list_container, songsFragment)
        transaction.commit()
    }
}