package com.example.musicplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Code::class, Song::class, Playlist::class, Likes::class], version = 13)
@TypeConverters(MyTypeConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun codeDao(): CodeDao
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun likesDao(): LikesDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "my_database")
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}