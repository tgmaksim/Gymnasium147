package ru.tgmaksim.gymnasium.utilities

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object CacheManager {

    private const val PREFS_NAME = "gymnasium_prefs"
    private const val KEY_THEME = "theme_dark"
    private const val KEY_OPENED_ACTIVITY = "opened_activity"
    private const val KEY_API_SESSION = "api_session"
    private const val KEY_SCHEDULE = "schedule"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_THEME, false)
        set(value) = prefs.edit { putBoolean(KEY_THEME, value) }

    var openedActivity: Int
        get() = prefs.getInt(KEY_OPENED_ACTIVITY, 0)
        set(value) = prefs.edit { putInt(KEY_OPENED_ACTIVITY, value) }

    var apiSession: String?
        get() = prefs.getString(KEY_API_SESSION, null)
        set(value) = prefs.edit { putString(KEY_API_SESSION, value) }

    var schedule: String?
        get() = prefs.getString(KEY_SCHEDULE, null)
        set(value) = prefs.edit { putString(KEY_SCHEDULE, value) }
}