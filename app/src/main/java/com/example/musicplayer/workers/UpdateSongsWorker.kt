package com.example.musicplayer.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.repositories.SongsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UpdateSongsWorker(private val appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    private lateinit var mDatabase: AppDatabase
    private lateinit var repository: SongsRepository
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage

    private val TAG = "UpdateSongsWorker"
    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.

        Log.d(TAG, "UpdateSongsWorker doWork()")

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(appContext)
        repository = SongsRepository(mDatabase.songDao(), mDatabase.likesDao(), SongsFirebase(firebaseDatabase, firebaseStorage))


        updateSongs()
        updateLikes()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun updateSongs(){
        GlobalScope.launch {
            val songs = repository.songsFirebase.readSongsUsingCoroutines(FirebaseConsts.songsDatabaseRef)
            songs?.let {
                Log.d(TAG, "Update songs. Count: ${it.size}")
                repository.songDao.deleteAllSongs()
                repository.songDao.insertAllSongs(songs)
            }
        }
    }

    private fun updateLikes(){
        GlobalScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user.uid
            val likes = repository.songsFirebase.loadLikes("${FirebaseConsts.likesDatabaseRef}/${userId}")
            likes?.let {
                Log.d(TAG, "Update likes. Count: ${it.songs!!.size}")
                repository.likesDao.deleteLikes()
                repository.likesDao.insertLikes(it)
            }
        }
    }
}