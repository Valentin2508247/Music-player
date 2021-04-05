package com.example.musicplayer.di

import com.example.musicplayer.activities.MainActivity
import com.example.musicplayer.modules.ApplicationModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class]) // все компоненты приложения
interface ApplicationComponent {
    // Activities
    fun inject(activity: MainActivity)

    // Fragments
}
