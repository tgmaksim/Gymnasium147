package ru.tgmaksim.gymnasium.utilities

import android.content.Context
import androidx.core.content.edit
import android.content.SharedPreferences

import ru.tgmaksim.gymnasium.api.VersionsResult

/**
 * Singleton для хранения значений в SharedPreferences (кеш)
 * @author Максим Дрючин (tgmaksim)
 * */
object CacheManager {
    private lateinit var prefs: SharedPreferences

    private const val PREFS_NAME = "gymnasium_prefs"
    private const val KEY_THEME = "theme_dark"
    private const val KEY_API_SESSION = "api_session"
    private const val KEY_SCHEDULE = "schedule"
    private const val KEY_OPEN_WEBVIEW = "open_WebView"
    private const val KEY_EA_NOTIFICATIONS = "extracurricular_activities"
    private const val KEY_FIREBASE_MESSAGING_TOKEN = "firebase_messaging_token"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Сохраненная тема приложения
     * */
    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_THEME, false)
        set(value) = prefs.edit { putBoolean(KEY_THEME, value) }

    /**
     * Сессия для API-запросов
     * */
    var apiSession: String?
        get() = prefs.getString(KEY_API_SESSION, null)
        set(value) = prefs.edit { putString(KEY_API_SESSION, value) }

    /**
     * Расписание уроков и внеурочных занятий в JSON-формате
     * */
    var schedule: String?
        get() = prefs.getString(KEY_SCHEDULE, null)
        set(value) = prefs.edit { putString(KEY_SCHEDULE, value) }

    /**
     * Настройка открытия прикрепленных файлов к домашнему заданию.
     * Если [openWebView] включена (по умолчанию),
     * то документы открываются в приложении, иначе — в браузере
     * */
    var openWebView: Boolean
        get() = prefs.getBoolean(KEY_OPEN_WEBVIEW, true)
        set(value) = prefs.edit { putBoolean(KEY_OPEN_WEBVIEW, value) }

    /**
     * Статус текущей версии (не сохраняется в кеше)
     * */
    var versionStatus: VersionsResult? = null

    /**
     * Настройка уведомлений о скором начале внеурочного занятия, по умолчанию - true
     * */
    var EANotifications: Boolean
        get() = prefs.getBoolean(KEY_EA_NOTIFICATIONS, true)
        set(value) = prefs.edit { putBoolean(KEY_EA_NOTIFICATIONS, value) }

    /**
     * Token для работы FirebaseMessaging
     * */
    var firebaseMessagingToken: String?
        get() = prefs.getString(KEY_FIREBASE_MESSAGING_TOKEN, null)
        set(value) = prefs.edit { putString(KEY_FIREBASE_MESSAGING_TOKEN, value) }
}