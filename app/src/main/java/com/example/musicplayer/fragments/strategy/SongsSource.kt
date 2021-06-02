package com.example.musicplayer.fragments.strategy

sealed class SongsSource {
    class AllSongs(): SongsSource()

    class PlaylistSongs(): SongsSource()

    class LikedSongs(): SongsSource()

    companion object{
        fun getSongSource(code: Int): SongsSource{
            return when (code){
                0 -> AllSongs()
                1 -> PlaylistSongs()
                2 -> LikedSongs()
                else -> AllSongs()
            }
        }
    }
}