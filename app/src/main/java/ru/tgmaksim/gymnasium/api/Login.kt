package ru.tgmaksim.gymnasium.api

import android.content.Context
import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.BuildConfig
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager

@Serializable
data class LoginUrl(
    val loginUrl: String,
    val session: String
)

object Login {
    const val PATH_PREFIX = "login"
    const val PATH_LOGIN: String = "login"
    private var loginUrl: String? = null

    /** Получение ссылки для авторизации (запросом на сервер или из сохраненного значения) */
    private suspend inline fun getLoginUrl(): String {
        return if (loginUrl == null) {
            loginUrl = requestLogin()
            loginUrl!!
        } else {
            loginUrl!!
        }
    }

    /** Создание сессии и открытие ссылки для ее авторизации */
    suspend fun login(context: Context) = Utilities.openUrl(context, getLoginUrl())

    /** Заранее создание сессии и сохранение ссылки для авторизации
     *
     * При вызове [Login.login] сразу возвращается сохраненная ссылка */
    suspend fun prepareLogin() {
        getLoginUrl()
    }

    /**
     * Отправка запроса на создание сессии и получение ссылки для ее авторизации
     *
     * После — сохранение сессию в кеш и возврат ссылки для авторизации */
    private suspend fun requestLogin(): String {
        val apiSession = CacheManager.apiSession

        // Если сессия существует, то сервер может только авторизовать ее, а не создавать новую
        val loginUrl = if (apiSession == null) {
            Request.post<SimpleInputData, LoginUrl>(
                "$PATH_PREFIX/$PATH_LOGIN",
                SimpleInputData(BuildConfig.API_KEY)
            )
        } else {
            Request.post<SessionData, LoginUrl>(
                "$PATH_PREFIX/$PATH_LOGIN",
                SessionData(BuildConfig.API_KEY, CacheManager.apiSession!!)
            )
        }

        CacheManager.apiSession = loginUrl.session
        return loginUrl.loginUrl
    }
}