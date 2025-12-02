package ru.tgmaksim.gymnasium.utilities

import android.content.Context
import androidx.core.content.edit
import android.content.SharedPreferences

object CacheManager {
    private lateinit var prefs: SharedPreferences

    private const val PREFS_NAME = "gymnasium_prefs"
    private const val KEY_THEME = "theme_dark"
    private const val KEY_API_SESSION = "api_session"
    private const val KEY_SCHEDULE = "schedule"
    private const val KEY_OPEN_WEBVIEW = "open_WebView"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Очищает весь сохраненный кеш */
    fun clear() {
        prefs.all.clear()
    }

    /** Сохраненная тема приложения */
    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_THEME, false)
        set(value) = prefs.edit { putBoolean(KEY_THEME, value) }

    /** Сессия для API-запросов */
    var apiSession: String?
        get() = prefs.getString(KEY_API_SESSION, null)
        set(value) = prefs.edit { putString(KEY_API_SESSION, value) }

    /** Расписание уроков и внеурочек */
    var schedule: String?
        get() = prefs.getString(KEY_SCHEDULE, null)
        set(value) = prefs.edit { putString(KEY_SCHEDULE, value) }

    var openWebView: Boolean
        get() = prefs.getBoolean(KEY_OPEN_WEBVIEW, true)
        set(value) = prefs.edit { putBoolean(KEY_OPEN_WEBVIEW, value) }
}