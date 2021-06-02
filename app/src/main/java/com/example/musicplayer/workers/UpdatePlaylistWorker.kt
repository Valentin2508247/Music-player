package com.example.musicplayer.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.firebase.PlaylistsFirebase
import com.example.musicplayer.repositories.PlaylistRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UpdatePlaylistWorker(private val appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {

    private lateinit var mDatabase: AppDatabase
    private lateinit var repository: PlaylistRepository
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage

    private val TAG = "UpdateSongsWorker"
    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.

        Log.d(TAG, "UpdateSongsWorker doWork()")

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(appContext)
        repository = PlaylistRepository(mDatabase.playlistDao(), PlaylistsFirebase(firebaseDatabase, firebaseStorage))


        updatePlaylists()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun updatePlaylists(){
        GlobalScope.launch {
            val playlists = repository.playlistsFirebase.readPlaylistsUsingCoroutines(FirebaseConsts.playlistsDatabaseRef)
            playlists?.let{
                Log.d(TAG, "Deleting all playlists")
                repository.playlistDao.deleteAllPlaylists()
                Log.d(TAG, "Inserting playlists")
                repository.playlistDao.insertAllPlaylists(playlists)
                Log.d(TAG, "Count: ${it.size}")
            }
        }
    }

}