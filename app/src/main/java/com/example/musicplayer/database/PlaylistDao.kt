package com.example.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaylistDao {

    @Query ("Select * from playlist")
    fun getAllPlaylists(): LiveData<List<Playlist>>

    @Query("Select * from playlist where id = :id")
    fun getPlaylist(id: String): Playlist

    @Query("Delete from playlist")
    fun deleteAllPlaylists()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllPlaylists(playlist: List<Playlist>)

    @Delete
    fun deletePlaylist(playlist: Playlist)


}