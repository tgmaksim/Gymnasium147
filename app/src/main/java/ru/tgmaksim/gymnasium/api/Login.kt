package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Запрос на генерацию сессии и получение ссылки для ее авторизации
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
 * Результат запроса на генерацию сессии и получение ссылки для ее авторизации
 * @param classId Идентификатор класса
 * @param loginUrl Ссылка для авторизации сессии (нужно открыть в браузере пользователя)
 * @param session Строковый идентификатор сессии для персонализированных запросов
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class LoginResult(
    override val classId: Int = CLASS_ID,
    val loginUrl: String,
    val session: String
) : ApiBase() {
    companion object {
        const val CLASS_ID = 0x00000008
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * Ответ на запрос на генерацию сессии и получение ссылки для ее авторизации
 * @property classId Идентификатор класса
 * @property status Статус выполненного запроса
 * @property error Объект API-ошибки
 * @property answer Ответ в случае успешной обработки
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class LoginApiResponse(
    override val classId: Int = CLASS_ID,
    override val status: Boolean,
    override val error: ApiError?,
    override val answer: LoginResult?
) : ApiResponse() {
    companion object {
        const val CLASS_ID = 0x00000009
    }
    init {
        if (classId != CLASS_ID && classId != ApiResponse.CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * API-singleton для запросов группы login
 * @property PATH_PREFIX Группа API-запросов
 * @property PATH_LOGIN Название API-запроса для авторизации
 * @author Максим Дрючин (tgmaksim)
 * */
object Login {
    private const val PATH_PREFIX = "login"
    private const val PATH_LOGIN: String = "login"

    /**
     * Создание сессии или повторная авторизация. Полученная сессия сохраняется в кеш
     * @return Ответ сервера в виде [LoginApiResponse]
     * @exception Exception
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend fun login(): LoginApiResponse {
        val request = LoginApiRequest(data = ApiSession(session = CacheManager.apiSession.toString()))

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