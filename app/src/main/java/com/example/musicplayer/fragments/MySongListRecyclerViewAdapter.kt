package com.example.musicplayer.fragments

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.database.Likes
import com.example.musicplayer.database.Song

class MySongListRecyclerViewAdapter(
        private var context: Context,
        private var data: List<Song>,
        private var listener: OnSongListItemClickListener,
        //private var likes: Likes
        private var likes: HashMap<String, Boolean> = HashMap<String, Boolean>()

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

    fun setData(songs: List<Song>, likes: Likes){
        data = songs
        if (likes.songs != null) {
            this.likes = HashMap<String, Boolean>(likes.songs!!)
        }
        else{
            this.likes = HashMap<String, Boolean>()
        }
        notifyDataSetChanged()
    }

    fun setLikes(likes: Likes){
        if (likes.songs != null) {
            this.likes = HashMap<String, Boolean>(likes.songs!!)
        }
        else{
            this.likes = HashMap<String, Boolean>()
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songIcon: ImageView = view.findViewById(R.id.song_icon)
        val songName: TextView = view.findViewById(R.id.song_name)
        val performer: TextView = view.findViewById(R.id.song_performer)
        //val duration: TextView = view.findViewById(R.id.song_duration)
        val likeIcon: ImageView = view.findViewById(R.id.like_icon)


        fun bind(song: Song, listener: OnSongListItemClickListener)
        {
            songName.text = song.songName
            performer.text = song.performer

            if (likes.containsKey(song.id)){
                //liked
                likeIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_favorite_white_24))
            }
            else {
                // not liked
                //likeIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like))
                likeIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_favorite_border_white_24))
            }

            song.imageUrl?.let {
                Glide.with(context)
                    .load(song.imageUrl)
                    .into(this.songIcon)
            }

            this.itemView.setOnClickListener {
                //listener.onSongListItemClicked(song)



                val pos: Int = data.indexOf(song)
                listener.playSongs(pos, data)
            }

            this.likeIcon.setOnClickListener {
                if (likes.containsKey(song.id))
                {
                    likes.remove(song.id)
                    likeIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_favorite_border_white_24))
                    //unlike song
                }
                else{
                    likes[song.id] = true
                    likeIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_favorite_white_24))
                }
                listener.onSongLiked(song, likes)
            }
        }
    }

    interface OnSongListItemClickListener{
        fun onSongListItemClicked(song: Song)
        fun onSongLiked(song: Song, likes: HashMap<String, Boolean>)
        fun playSongs(pos: Int, data: List<Song>)
    }
}