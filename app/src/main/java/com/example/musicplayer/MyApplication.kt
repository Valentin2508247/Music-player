package com.example.musicplayer

import android.app.Application
import com.example.musicplayer.di.ApplicationComponent


class MyApplication: Application() {
//    lateinit var appComponent: ApplicationComponent //= DaggerApplicationComponent.create()

    override fun onCreate() {
        super.onCreate()
//        appComponent = DaggerApplicationComponent
//                .builder()
//                .build()
    }
}