package com.example.musicplayer

import android.app.Application
import com.example.musicplayer.di.ApplicationComponent
import com.example.musicplayer.services.UpdateSongsIntentService


class MyApplication: Application() {
//    lateinit var appComponent: ApplicationComponent //= DaggerApplicationComponent.create()

    override fun onCreate() {
        super.onCreate()
        //UpdateSongsIntentService.startActionUpdateSongs(applicationContext, "param1", "param2")

//        appComponent = DaggerApplicationComponent
//                .builder()
//                .build()
    }
}