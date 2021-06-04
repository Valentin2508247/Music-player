package com.example.musicplayer.firebase

import android.net.Uri
import android.util.Log
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class PlaylistsFirebase(private val mDatabase: FirebaseDatabase, private val mStorage: FirebaseStorage) {
    private val TAG = "PlaylistsFirebase"

    suspend fun uploadPlaylist(playlist: Playlist, isShared: Boolean){
        var reference = mDatabase.reference
        if (isShared)
            reference = reference.child(FirebaseConsts.playlistsDatabaseRef)
        else {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user.uid
            reference = reference.child("${FirebaseConsts.privatePlaylistsDatabaseRef}/${userId}")
        }
        val key = reference.push().key

        if (key == null) {
            Log.w(TAG, "Couldn't get push key for posts")
            return
        }
        playlist.id = key

        uploadImageToStorage(playlist.imagePath!!)
        playlist.imagePath = Uri.fromFile(File(playlist.imagePath)).lastPathSegment
        reference.child(key).setValue(playlist)
    }

    private suspend fun uploadImageToStorage(path: String){
        var file = Uri.fromFile(File(path))
        val reference = mStorage.reference.child("${FirebaseConsts.playlistIconStorage}/${file.lastPathSegment}")
        val uploadTask = reference.putFile(file)
        uploadTask.await()
    }

    suspend fun readPlaylistsUsingCoroutines(path: String): List<Playlist>?{

            var playlists: List<Playlist> = emptyList<Playlist>()


            val reference = mDatabase.reference.child(path)
            val snapshot = reference.get().await()

            //Log.d(TAG, snapshot.toString())

            snapshot?: return null

            playlists = snapshot.children.map { snapShot ->
//            Log.d(TAG, " ")
//            Log.d(TAG, "#######################")
//            Log.d(TAG, snapShot.toString())
                val playlist = snapShot.getValue(Playlist::class.java)!!
                playlist.id = snapShot.key?:""
//            Log.d(TAG, "Playlist: $playlist")
//            Log.d(TAG, "#######################")
//            Log.d(TAG, " ")
                playlist
            }

            for (playlist: Playlist in playlists){
                try{
                    loadPlaylistImageUrl(playlist)
                }
                catch (exception: Exception)
                {
                    Log.e(TAG, "Something went wrong")
                    Log.e(TAG, exception.message)
                    continue
                }
            }

            for (playlist in playlists){
                Log.d(TAG, playlist.toString())
            }


            return playlists
    }

    private suspend fun loadPlaylistImageUrl(playlist: Playlist): String {
        val path = "${FirebaseConsts.playlistIconStorage}/${playlist.imagePath}"
        //Log.d(TAG, path)
        val reference = mStorage.reference.child(path)
        val url = reference.downloadUrl.await().toString()
        //Log.d(TAG, "Url: $url")
        playlist.imageUrl = url
        //Log.d(TAG, "Song: $song")
        return url
    }

    fun updatePlaylist(playlist: Playlist, id: String){
        val reference = mDatabase.reference.child("${FirebaseConsts.playlistsDatabaseRef}/${id}")

        reference.setValue(playlist)
    }

}