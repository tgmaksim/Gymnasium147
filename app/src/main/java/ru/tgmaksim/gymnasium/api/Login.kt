package ru.tgmaksim.gymnasium.api

import android.content.Context
import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Data-класс для результата API-запроса /[Login.PATH_PREFIX]/[Login.PATH_LOGIN]/.
 * @param loginUrl Ссылка для авторизации
 * @param session Сгенерированная сессия для дальнейших персонализированных API-запросов
 * @author Максим Дрючин (tgmaksim)
 * @see Login.requestLogin
 * */
@Serializable private data class LoginUrl(
    val loginUrl: String,
    val session: String
)

/**
 * API-singleton для авторизации пользователя
 * @property PATH_PREFIX Группа API-запросов
 * @property PATH_LOGIN Название API-запроса для авторизации
 * @author Максим Дрючин (tgmaksim)
 * */
object Login {
    private const val PATH_PREFIX = "login"
    private const val PATH_LOGIN: String = "login"
    private var loginUrl: String? = null

    /**
     * Получение ссылки для авторизации запросом на сервер.
     * Если ранее ссылка была получена и сохранена, то она сразу возвращается.
     * @return ссылка для авторизации
     * @author Максим Дрючин (tgmaksim)
     * */
    private suspend inline fun getLoginUrl(): String =
        if (loginUrl == null) {
            loginUrl = requestLogin()
            loginUrl!!
        } else {
            loginUrl!!
        }

    /**
     * Создание сессии (если еще не создана) и открытие ссылки для ее авторизации
     * @param context Android-context для открытия ссылки в браузере
     * @see Login.getLoginUrl
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend fun login(context: Context) = Utilities.openUrl(context, getLoginUrl())

    /**
     * Заранее создание сессии и сохранение ссылки для авторизации.
     * @author Максим Дрючин (tgmaksim)
     * @see Login.getLoginUrl
     * */
    suspend fun prepareLogin() {
        getLoginUrl()
    }

    /**
     * API-запрос /[Login.PATH_PREFIX]/[Login.PATH_LOGIN]/ с результатом [LoginUrl]
     * на создание сессии и получение ссылки для ее авторизации.
     * Полученная сессия сохраняется в кеш
     * @return ссылка для авторизации
     * @author Максим Дрючин (tgmaksim)
     * */
    private suspend fun requestLogin(): String {
        val apiSession = CacheManager.apiSession

        // Если сессия существует, то сервер может только авторизовать ее, а не создавать новую
        val loginUrl = if (apiSession == null) {
            Request.post<SimpleInputData, LoginUrl>(
                listOf(PATH_PREFIX, PATH_LOGIN).joinToString("/"),
                SimpleInputData()
            )
        } else {
            Request.post<SessionData, LoginUrl>(
                listOf(PATH_PREFIX, PATH_LOGIN).joinToString("/"),
                SessionData(apiSession)
            )
        }

        // Сохранение сессии в кеш
        CacheManager.apiSession = loginUrl.session

        return loginUrl.loginUrl
    }
}