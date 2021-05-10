package com.example.musicplayer.firebase

import android.net.Uri
import android.util.Log
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.suspendCoroutine

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
}