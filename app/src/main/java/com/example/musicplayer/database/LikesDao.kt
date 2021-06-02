package com.example.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LikesDao {

    @Query("Select * from likes")
    fun getAllLikes(): LiveData<Likes>

    @Query("Delete from likes")
    fun deleteLikes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLikes(likes: Likes)

    @Transaction
    fun insertAndDelete(likes: Likes){
        deleteLikes()
        insertLikes(likes)
    }
//    @Delete
//    fun deleteLikes(playlist: Playlist)
}