package com.example.musicplayer.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Song (var id: String?, var songName: String?, var performer: String?,
    var imagePath: String?, var songPath: String?, var url: String?){

    constructor(): this("", "", "", "", "", "") {}
}
