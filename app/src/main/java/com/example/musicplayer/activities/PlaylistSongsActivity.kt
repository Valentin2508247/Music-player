package com.example.musicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.R
import com.example.musicplayer.fragments.SongListFragment

class PlaylistSongsActivity : AppCompatActivity() {

    private lateinit var playlistId: String
    private lateinit var songsFragment: SongListFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_songs)



        playlistId = intent.getStringExtra(SongListFragment.ARG_PLAYLIST)
        Toast.makeText(this, "Playlist id: $playlistId", Toast.LENGTH_LONG).show()
        createFragment()

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            openActivity()
        }
    }

    private fun createFragment() {
        songsFragment = SongListFragment.newInstance(1, 1, playlistId)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_playlist_songs, songsFragment)
        transaction.commit()
    }

    private fun openActivity(){
        intent = Intent(this, AddSongsToPlaylistActivity::class.java)
        intent.putExtra("playlist", playlistId)
        startActivity(intent)
    }
}