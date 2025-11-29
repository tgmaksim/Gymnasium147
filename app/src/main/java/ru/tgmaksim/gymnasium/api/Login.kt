package ru.tgmaksim.gymnasium.api

import android.content.Context
import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager

@Serializable
data class LoginUrl(val loginUrl: String, val session: String)

@Serializable
data class SessionStatus(val exists: Boolean, val auth: Boolean)

object Login {
    const val PATH_LOGIN: String = "login"
    const val PATH_CHECK_SESSION = "checkSession"
    private var loginUrl: String? = null

    /**
     * Отправляет запрос на создание сессии и получение ссылки для ее авторизации
     *
     * Сохраняет сессию в кеш и возвращает ссылку для авторизации */
    private suspend fun getLoginUrl(): String {
        if (loginUrl != null)
            return loginUrl!!

        val loginUrl: LoginUrl = Request.post<SimpleInputData, LoginUrl>(
            PATH_LOGIN,
            SimpleInputData(Constants.API_KEY)
        )
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

    /**
     * Проверяет существование и авторизацию сессии
     *
     * Возвращает [SessionStatus] с параметрами [SessionStatus.exists] - существование сессии и
     * [SessionStatus.auth] - авторизация сессии */
    suspend fun checkSession(): SessionStatus {
        if (CacheManager.apiSession?.isEmpty() != false)
            // При отсутствии сессии сразу возвращает результат
            return SessionStatus(exists = false, auth = false)

        return Request.post<SessionData, SessionStatus>(
            PATH_CHECK_SESSION,
            SessionData(Constants.API_KEY, CacheManager.apiSession!!)
        )
    }
}