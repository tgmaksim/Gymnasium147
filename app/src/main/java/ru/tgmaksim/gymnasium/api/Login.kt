package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Data-класс для запроса создания сессии и последующей авторизации
 * @param classId Идентификатор класса
 * @param data Данные сессии для повторной авторизации без создания новой (может быть отклонено и создана новая)
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class LoginApiRequest(
    override val classId: Int = CLASS_ID,
    override val data: ApiSession?
) : ApiRequest() {
    companion object {
        const val CLASS_ID = 0x00000007
    }
}

/**
 * Data-класс для результата запроса проверки версии
 * @param classId Идентификатор класса
 * @param loginUrl Ссылка для авторизации сессии (нужно открыть в браузере пользователя)
 * @param session Строковый идентификатор сессии для персонализированных запросов
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class LoginResult(
    override val classId: Int,
    val loginUrl: String,
    val session: String
) : ApiBase() {
    companion object {
        private const val CLASS_ID = 0x00000008
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * Data-класс для ответа на запрос проверки версии
 * @property classId Идентификатор класса
 * @property status Статус выполненного запроса
 * @property error Объект ошибки
 * @property answer Ответ в случае успешной обработки
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class LoginApiResponse(
    override val classId: Int,
    override val status: Boolean,
    override val error: ApiError?,
    override val answer: LoginResult?
) : ApiResponse() {
    companion object {
        private const val CLASS_ID = 0x00000009
    }
    init {
        if (classId != CLASS_ID && classId != ApiResponse.CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * API-singleton для авторизации пользователя
 * @property PATH_PREFIX Группа API-запросов
 * @property PATH_LOGIN Название API-запроса для авторизации
 * @author Максим Дрючин (tgmaksim)
 * */
object Login {
    private const val PATH_PREFIX = "login"
    private const val PATH_LOGIN: String = "login"

    /**
     * API-запрос /[Login.PATH_PREFIX]/[Login.PATH_LOGIN]/ с результатом [LoginResult]
     * на создание сессии и получение ссылки для ее авторизации.
     * Полученная сессия сохраняется в кеш
     * @return ссылка для авторизации
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend fun login(): LoginApiResponse {
        val request = LoginApiRequest(data = CacheManager.apiSession?.let { ApiSession(session = it) })

        val response = Request.post<LoginApiRequest, LoginApiResponse>(
            listOf(PATH_PREFIX, PATH_LOGIN, LoginApiRequest.CLASS_ID).joinToString("/"),
            request
        )

        // Сохранение сессии в кеш
        response.answer?.let {
            CacheManager.apiSession = it.session
        }

        return response
    }
}