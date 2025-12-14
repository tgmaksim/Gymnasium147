package ru.tgmaksim.gymnasium

import android.app.Application

import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Класс приложения для инициализации
 * @author Максим Дрючин (tgmaksim)
 * */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Загрузка CacheManager'а один раз
        CacheManager.init(this)
    }
}