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
import com.example.musicplayer.database.Song

import com.example.musicplayer.fragments.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MySongListRecyclerViewAdapter(
        private var context: Context,
        private var data: List<Song>,
        private var listener: OnSongListItemClickListener
) : RecyclerView.Adapter<MySongListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_song_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = data[position]

        holder.bind(song, listener)
        // TODO: deal with icon
//        song.imageUrl?.let {
//            Glide.with(context)
//                    .load(song.imageUrl)
//                    .into(holder.songIcon)
//        }
//
//        //holder.songIcon
//        holder.songName.text = song.songName
//        holder.performer.text = song.performer
        // TODO: deal with song duration
        //holder.duration
    }

    override fun getItemCount(): Int = data.size

    fun setData(songs: List<Song>){
        data = songs
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songIcon: ImageView = view.findViewById(R.id.song_icon)
        val songName: TextView = view.findViewById(R.id.song_name)
        val performer: TextView = view.findViewById(R.id.song_performer)
        val duration: TextView = view.findViewById(R.id.song_duration)


        fun bind(song: Song, listener: OnSongListItemClickListener)
        {
            songName.text = song.songName
            performer.text = song.performer

            song.imageUrl?.let {
                Glide.with(context)
                    .load(song.imageUrl)
                    .into(this.songIcon)
            }

            this.itemView.setOnClickListener {
                listener.onSongListItemClicked(song)
            }
        }

        override fun toString(): String {
            return super.toString()
        }
    }

    interface OnSongListItemClickListener{
        fun onSongListItemClicked(song: Song)
    }
}