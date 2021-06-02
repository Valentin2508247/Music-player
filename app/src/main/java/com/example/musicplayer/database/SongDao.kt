package com.example.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SongDao {

    @Query("Select * from song")
    fun getAllSongs(): LiveData<List<Song>>

    @Query("Select * from song")
    fun getSongs(): List<Song>

    @Query("Select * from song where id in (:ids)")
    fun getSongsByIds(ids: List<String>): LiveData<List<Song>>

    @Query("Select * from song where id = :id")
    fun getSong(id: String): Song

    @Query("Delete from song")
    fun deleteAllSongs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSongs(songs: List<Song>)

    @Delete
    fun deleteSong(song: Song)

//    // can't return LiveData from transaction
//    @Transaction
//    fun getLikedSongs(): LiveData<List<Song>>{
//        val likes = getLikes()
//        val list = likes.songs!!.keys.toList()
//        return getSongsByIds(list)
//    }
//
    @Query("Select * from likes")
    fun getLikes(): Likes
}