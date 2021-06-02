package com.example.musicplayer

import android.app.Application
import androidx.work.*
import com.example.musicplayer.di.ApplicationComponent
import com.example.musicplayer.workers.UpdatePlaylistWorker
import com.example.musicplayer.workers.UpdateSongsWorker
import java.util.concurrent.TimeUnit


class MyApplication: Application() {
//    lateinit var appComponent: ApplicationComponent //= DaggerApplicationComponent.create()

    override fun onCreate() {
        super.onCreate()

//        var workManager = WorkManager.getInstance(this)
//
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//
//
//        val uploadSongsWorkRequest = OneTimeWorkRequestBuilder<UpdateSongsWorker>()
//            .setConstraints(constraints)
//            .build()
//
//        val uploadPlaylistsWorkRequest = OneTimeWorkRequestBuilder<UpdatePlaylistWorker>()
//                .setConstraints(constraints)
//                .build()
//
////        val uploadSongsWorkRequest = PeriodicWorkRequestBuilder<UpdateSongsWorker>(24, TimeUnit.DAYS)
////            .setConstraints(constraints)
////            .build()
//
//        workManager.enqueue(uploadSongsWorkRequest)
//        workManager.enqueue(uploadPlaylistsWorkRequest)
//        //UpdateSongsIntentService.startActionUpdateSongs(applicationContext, "param1", "param2")
//
////        appComponent = DaggerApplicationComponent
////                .builder()
////                .build()
    }
}