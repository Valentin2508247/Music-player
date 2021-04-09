package com.example.musicplayer.firebase

import android.util.Log
import com.example.musicplayer.database.Song
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SongsFirebase(private val mDatabase: FirebaseDatabase) {
    private val TAG = "SongsFirebase"

    fun readSongs(path: String, songsUpdater: IUpdateSongs) {
        val reference = mDatabase.reference.child(path)
//        return RxFirebaseDatabase.observeSingleValueEvent(reference, DataSnapshotMapper.listOf(Song::class.java))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
        Log.d(TAG, "read songs method start")
        RxFirebaseDatabase.observeSingleValueEvent(reference, DataSnapshotMapper.listOf(Song::class.java))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                // onSuccess
                Log.d(TAG, "onSuccess")
                songsUpdater.updateSongs(it)
            },
            {
                // onError
                Log.d(TAG, "onError")
                Log.d(TAG, it.message)
                it.printStackTrace()
                throw it
            },
            {
                // onComplete
                Log.d(TAG, "onComplete")
            }).dispose()

//        RxFirebaseDatabase.observeSingleValueEvent(reference)
//        {
//            Log.d(TAG, "Observe")
//            val list: MutableList<Song> = ArrayList<Song>()
//            for (childSnap: DataSnapshot in it.children) {
//                val song: Song? = childSnap.getValue(Song::class.java)
//                song ?: continue
//                Log.d(TAG, "Song $song")
//                song.id = childSnap.key!!
//                list.add(song)
//            }
//            return@observeSingleValueEvent list
//        }
//        .subscribeOn(Schedulers.io())
//        .observeOn(Schedulers.io())
//        .subscribe({
//            // onSuccess
//            Log.d(TAG, "onSuccess")
//            songsUpdater.updateSongs(it)
//        },
//        {
//            // onError
//            Log.d(TAG, "onError")
//            Log.d(TAG, it.message)
//            it.printStackTrace()
//        },
//        {
//            // onComplete
//            Log.d(TAG, "onComplete")
//        }).dispose()
    }

    interface IUpdateSongs{
        fun updateSongs(songs: MutableList<Song>)
    }
}