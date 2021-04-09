package com.example.musicplayer.fragments

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.database.Playlist

import com.example.musicplayer.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyPlaylistRecyclerViewAdapter(
    private val values: List<Playlist>
) : RecyclerView.Adapter<MyPlaylistRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_playlist_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = values[position]
        // TODO: set icon of playlist
        // holder.playlistIcon
        holder.playlistName.text = playlist.name
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistIcon: ImageView = view.findViewById(R.id.playlist_icon)
        val playlistName: TextView = view.findViewById(R.id.playlist_name)
    }
}