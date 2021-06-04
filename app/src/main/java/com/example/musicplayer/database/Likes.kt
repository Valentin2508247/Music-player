package com.example.musicplayer.database
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable


@IgnoreExtraProperties
@Entity(tableName = "likes")
data class Likes(@Exclude @PrimaryKey var id: String,
                 var songs: Map<String, Boolean>?): Serializable {

    constructor(): this("qwerty", HashMap<String, Boolean>())
}