package ru.tgmaksim.gymnasium

import android.app.Application
import com.google.firebase.FirebaseApp
import ru.tgmaksim.gymnasium.api.API_KEY
import com.google.firebase.messaging.FirebaseMessaging

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Класс приложения для инициализации
 * @author Максим Дрючин (tgmaksim)
 * */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Загрузка CacheManager'а
        CacheManager.init(this)

        API_KEY = BuildConfig.API_KEY

        // Загрузка Firebase
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Utilities.log(task.exception ?: Exception("Fetching FCM registration token failed"))
                return@addOnCompleteListener
            }

            CacheManager.firebaseMessagingToken = task.result
        }
    }
}