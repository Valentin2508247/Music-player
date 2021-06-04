package com.example.musicplayer.repositories

import com.example.musicplayer.database.PlaylistDao
import com.example.musicplayer.firebase.PlaylistsFirebase

class PlaylistRepository(val playlistDao: PlaylistDao, val playlistsFirebase: PlaylistsFirebase) {
}