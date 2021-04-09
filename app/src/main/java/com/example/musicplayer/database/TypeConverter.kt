package com.example.musicplayer.database

import java.lang.StringBuilder
import androidx.room.TypeConverter

class MyTypeConverter {
    @TypeConverter // "songId1|songId2|"
    fun fromString(value: String?): Map<String, Boolean> {
        val map = HashMap<String, Boolean>()
        value?.let {
            val str = value.substring(0, value.length - 1)
            for (name: String in str.split("|"))
                map[name] = true
        }
        return map
    }

    @TypeConverter //[songId: true, ...]
    fun toString(map: Map<String, Boolean>): String {
        var sb = StringBuilder()
        for ((k, _) in map){
            sb.append(k)
            sb.append('|')
        }
        return sb.toString()
    }
}