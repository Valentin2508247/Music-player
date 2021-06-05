package com.example.musicplayer.firebase

class FirebaseConsts {
    companion object{
        const val songsDatabaseRef: String = "songs"
        const val playlistsDatabaseRef: String = "publicPlaylists"
        const val privatePlaylistsDatabaseRef: String = "playlists"
        const val logs: String = "logs"
        const val likesDatabaseRef: String = "likes"

        const val songIconStorage: String = "images"
        const val songMusicStorage: String = "music"
        const val playlistIconStorage: String = "playlistImages"
    }
}