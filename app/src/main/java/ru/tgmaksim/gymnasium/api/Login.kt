package ru.tgmaksim.gymnasium.api

import android.content.Context
import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager

@Serializable
data class LoginUrl(val loginUrl: String, val session: String)

object Login {
    const val PATH_PREFIX = "login"
    const val PATH_LOGIN: String = "login"
    private var loginUrl: String? = null

    /**
     * Отправляет запрос на создание сессии и получение ссылки для ее авторизации
     *
     * Сохраняет сессию в кеш и возвращает ссылку для авторизации */
    private suspend fun getLoginUrl(): String {
        if (loginUrl != null)
            return loginUrl!!

        val loginUrl = if (CacheManager.apiSession == null) {
            Request.post<SimpleInputData, LoginUrl>(
                "$PATH_PREFIX/$PATH_LOGIN",
                SimpleInputData(Constants.API_KEY)
            )
        } else {
            Request.post<SessionData, LoginUrl>(
                "$PATH_PREFIX/$PATH_LOGIN",
                SessionData(Constants.API_KEY, CacheManager.apiSession!!)
            )
        }

        CacheManager.apiSession = loginUrl.session
        return loginUrl.loginUrl
    }

    /** Создает сессию и открывает ссылку ее авторизации */
    suspend fun login(context: Context) {
        val url: String = getLoginUrl()
        Utilities.openUrl(context, url)
    }

    /** Заранее создает сессию и сохраняет ссылку для авторизации
     *
     * При вызове [Login.login] сразу открывает сохраненную ссылку */
    suspend fun prepareLogin() {
        loginUrl = getLoginUrl()
    }
}