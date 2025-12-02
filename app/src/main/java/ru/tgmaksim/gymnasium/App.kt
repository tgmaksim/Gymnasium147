package ru.tgmaksim.gymnasium

import android.app.Application

import ru.tgmaksim.gymnasium.utilities.CacheManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Загрузка CacheManager'а один раз
        CacheManager.init(this)
    }
}