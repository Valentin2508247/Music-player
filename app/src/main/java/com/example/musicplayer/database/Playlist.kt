package com.example.musicplayer.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "playlist")
data class Playlist(@Exclude @PrimaryKey var id: String, var name: String?,
                    var imagePath: String?, var songs: Map<String, Boolean>?,
                    var imageUrl: String?): Serializable {

    constructor(): this("", "", "", null, null)
}