package com.example.musicplayer.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity (tableName = "song")
data class Song(@PrimaryKey var id: String,
    var songName: String?,
    var performer: String?,
    var imagePath: String?,
    var songPath: String?,
    var url: String?){

    constructor(): this("", "", "", "", "", "") {}
}
