package com.example.musicplayer.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "playlist")
class Playlist(@PrimaryKey var id: String, var name: String?,
    var imagePath: String?, var songs: Map<String, Boolean>?) {

    constructor(): this("", "", "", null)
}