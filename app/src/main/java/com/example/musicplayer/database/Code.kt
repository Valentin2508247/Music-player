package com.example.musicplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "code")
data class Code (
    @ColumnInfo(name = "code") var code: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "cost") var cost: Int,
    @ColumnInfo(name = "cities") var cities: String,
    @ColumnInfo(name = "isfood") var isFood: Boolean
)
{
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}
