package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.Serializable

var API_KEY = "00000000000000000000000000000000"

/**
 * Базовый класс для любой API-сущности
 * @property classId Идентификатор класса
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable abstract class ApiBase {
    protected abstract val classId: Int
}

/**
 * Базовый класс для всех API-запросов
 * @property classId Идентификатор класса
 * @property apiKey API-ключ для взаимодействия с сервером
 * @property data Входные данные запроса
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable abstract class ApiRequest : ApiBase() {
    abstract override val classId: Int
    val apiKey: String = API_KEY
    abstract val data: ApiBase?
}

/**
 * Data-класс для всех ошибок в API-ответах
 * @property classId Идентификатор класса
 * @property type Определенный тип ошибки из возможных
 * @property errorMessage Сообщение об ошибке для показа пользователю коротким оповещением
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class ApiError(
    override val classId: Int,
    val type: String,
    val errorMessage: String?
) : ApiBase() {
    companion object {
        private const val CLASS_ID = 0x00000001
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * Базовый класс для всех API-ответов
 * @property classId Идентификатор класса
 * @property status Статус выполненного запроса
 * @property error Краткое описание ошибки на сервере (только для логов)
 * @property answer Ответ в случае успешной обработки
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable abstract class ApiResponse : ApiBase() {
    abstract override val classId: Int
    abstract val status: Boolean
    abstract val error: ApiError?
    abstract val answer: ApiBase?

    companion object {
        protected const val CLASS_ID = 0x00000002
    }
}

/**
 * Данные сессии для персонализированных запросов
 * @param classId Идентификатор класса
 * @param session Строковый идентификатор сессии для персонализированных запросов
 * */
@Serializable data class ApiSession(
    override val classId: Int,
    val session: String
) : ApiBase() {
    companion object {
        private const val CLASS_ID = 0x00000003
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }

    constructor(session: String): this(CLASS_ID, session)
}