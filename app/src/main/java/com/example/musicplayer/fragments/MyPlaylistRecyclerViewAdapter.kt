package com.example.musicplayer.fragments

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song

import com.example.musicplayer.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyPlaylistRecyclerViewAdapter(
        private var context: Context,
        private var data: List<Playlist>,
        private var listener: OnPlaylistListItemClickListener
) : RecyclerView.Adapter<MyPlaylistRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_playlist_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = data[position]
        holder.bind(playlist, listener)
    }

    fun setData(playlists: List<Playlist>){
        data = playlists
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistIcon: ImageView = view.findViewById(R.id.playlist_icon)
        val playlistName: TextView = view.findViewById(R.id.playlist_name)

        fun bind(playlist: Playlist, listener: OnPlaylistListItemClickListener){
            playlistName.text = playlist.name
            playlist.imageUrl?.let {
                Glide.with(context)
                        .load(playlist.imageUrl)
                        .into(this.playlistIcon)
            }
            this.itemView.setOnClickListener {
                listener.onPlaylistItemClicked(playlist)
            }
        }
    }

    interface OnPlaylistListItemClickListener{
        fun onPlaylistItemClicked(playlist: Playlist)
    }
}