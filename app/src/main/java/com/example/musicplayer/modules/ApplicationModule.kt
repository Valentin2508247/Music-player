package com.example.musicplayer.modules

import android.app.Application
import android.content.Context
import com.example.musicplayer.MyApplication
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(val application: MyApplication, val context: Context) {

    @Provides
    fun provideApplication(): Application = application

    @Provides
    fun provideContext() = context
}