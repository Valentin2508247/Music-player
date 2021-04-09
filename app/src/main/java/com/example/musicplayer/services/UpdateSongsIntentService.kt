package com.example.musicplayer.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.firebase.SongsFirebase
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Maybe

// TODO: Rename actions, choose action names that describe tasks that this
// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
private const val ACTION_UPDATE_SONGS = "com.example.musicplayer.services.action.update_songs"
private const val ACTION_BAZ = "com.example.musicplayer.services.action.BAZ"

// TODO: Rename parameters
private const val EXTRA_PARAM1 = "com.example.musicplayer.services.extra.PARAM1"
private const val EXTRA_PARAM2 = "com.example.musicplayer.services.extra.PARAM2"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.

 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.

 */
class UpdateSongsIntentService : IntentService("UpdateSongsIntentService") {
    private val TAG = "UpdateSongsService"


    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_UPDATE_SONGS -> {
                Log.d(TAG, ACTION_UPDATE_SONGS)
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionUpdateSongs(param1, param2)
            }
            ACTION_BAZ -> {
                Log.d(TAG, ACTION_BAZ)
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionBaz(param1, param2)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionUpdateSongs(param1: String, param2: String) {
        val roomDatabase = AppDatabase.getDatabase(this)
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val songsFirebase = SongsFirebase(firebaseDatabase)

        Log.d(TAG, "handleActionUpdateSongs")

//        val maybe: Maybe<MutableList<Song>> = songsFirebase.readSongs(FirebaseConsts.songsRef)
//        maybe.subscribe({
//            // onSuccess
//            Log.d(TAG, "onSuccess")
//            roomDatabase.songDao().deleteAllSongs()
//            for (song: Song in it){
//                Log.d(TAG, "$song")
//                roomDatabase.songDao().insertSong(song)
//            }
//        },
//            {
//                // onError
//                Log.d(TAG, "onError")
//                Log.d(TAG, it.message)
//                it.printStackTrace()
//            },
//            {
//                // onComplete
//                Log.d(TAG, "onComplete")
//            }).dispose()
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        TODO("Handle action Baz")
    }

    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionUpdateSongs(context: Context, param1: String, param2: String) {
            Log.d("UpdateSongsService", "Service started")
            val intent = Intent(context, UpdateSongsIntentService::class.java).apply {
                action = ACTION_UPDATE_SONGS
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionBaz(context: Context, param1: String, param2: String) {
            val intent = Intent(context, UpdateSongsIntentService::class.java).apply {
                action = ACTION_BAZ
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }
    }
}