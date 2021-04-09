package com.example.musicplayer.repositories

import com.example.musicplayer.database.SongDao
import com.example.musicplayer.firebase.SongsFirebase

class SongsRepository(val songDao: SongDao, val songsFirebase: SongsFirebase){

}