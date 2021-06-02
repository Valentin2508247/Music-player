package com.example.musicplayer.firebase

import android.net.Uri
import android.util.Log
import com.example.musicplayer.database.Likes
import com.example.musicplayer.database.Song
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class SongsFirebase(private val mDatabase: FirebaseDatabase, private val mStorage: FirebaseStorage) {
    private val TAG = "SongsFirebase"

    suspend fun uploadSong(song: Song){
        val reference = mDatabase.reference.child(FirebaseConsts.songsDatabaseRef)
        val key = reference.push().key

        if (key == null) {
            Log.w(TAG, "Couldn't get push key for songs")
            return
        }
        song.id = key

        uploadFileToStorage(song.imagePath!!, FirebaseConsts.songIconStorage)
        uploadFileToStorage(song.songPath!!, FirebaseConsts.songMusicStorage)
        song.imagePath = Uri.fromFile(File(song.imagePath)).lastPathSegment
        song.songPath = Uri.fromFile(File(song.songPath)).lastPathSegment
        reference.child(key).setValue(song)
    }

    private suspend fun uploadFileToStorage(path: String, childRef: String){
        var file = Uri.fromFile(File(path))
        val reference = mStorage.reference.child("${childRef}/${file.lastPathSegment}")
        val uploadTask = reference.putFile(file)
        uploadTask.await()
    }

    suspend fun readSongsUsingCoroutines(path: String): List<Song>?{
        var songs: List<Song>
        val reference = mDatabase.reference.child(path)
        val snapshot = reference.get().await()

        //Log.d(TAG, snapshot.toString())

        snapshot?: return null

        songs = snapshot.children.map { snapShot ->
//            Log.d(TAG, " ")
//            Log.d(TAG, "#######################")
//            Log.d(TAG, snapShot.toString())
            val song = snapShot.getValue(Song::class.java)!!
            song.id = snapShot.key?:""
//            Log.d(TAG, "Song: $song")
//            Log.d(TAG, "#######################")
//            Log.d(TAG, " ")
            song
        }

        for (song: Song in songs){
            try{
                loadSongMusicUrl(song)
                loadSongImageUrl(song)
            }
            catch (exception: Exception)
            {
                Log.e(TAG, "Something went wrong")
                Log.e(TAG, exception.message)
                continue
            }
        }

//        for (song in songs){
//            Log.d(TAG, song.toString())
//        }

        return songs
    }

    private suspend fun loadSongImageUrl(song: Song): String{
        val path = "${FirebaseConsts.songIconStorage}/${song.imagePath}"
        //Log.d(TAG, path)
        val reference = mStorage.reference.child("${FirebaseConsts.songIconStorage}/${song.imagePath}")
        val url = reference.downloadUrl.await().toString()
        //Log.d(TAG, "Url: $url")
        song.imageUrl = url
        //Log.d(TAG, "Song: $song")
        return url
    }

    private suspend fun loadSongMusicUrl(song: Song): String{
        val path = "${FirebaseConsts.songMusicStorage}/${song.songPath}"
        //Log.d(TAG, path)
        val reference = mStorage.reference.child("${FirebaseConsts.songMusicStorage}/${song.songPath}")
        val url = reference.downloadUrl.await().toString()
        //Log.d(TAG, "Url: $url")
        song.songUrl = url
        return url
    }

    // songId: true
    suspend fun loadLikes(path: String): Likes {
        // TODO: deal with no likes

        val reference = mDatabase.reference.child(path)
        val snapshot = reference.get().await()
        snapshot?: return Likes()


        val likes = snapshot.getValue(Likes::class.java)


//        Log.d(TAG, "Count: ${map!!.keys.size}")

        likes?.id = snapshot.key?:""
        Log.d(TAG, "Likes: $likes")

        if (likes == null){
           return Likes()
        }

        Log.d(TAG, "Count: ${likes.songs?.keys?.size}")
        return likes
    }

    fun uploadLikes(likes: HashMap<String, Boolean>, path: String) {
        val myLikes: Likes = Likes("qwerty", likes)
        val reference = mDatabase.reference.child(path)
        reference.setValue(myLikes)
    }

}