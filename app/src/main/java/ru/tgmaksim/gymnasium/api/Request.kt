package ru.tgmaksim.gymnasium.api

import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.util.reflect.typeInfo
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.setBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

import ru.tgmaksim.gymnasium.BuildConfig
import kotlin.coroutines.cancellation.CancellationException

/**
 * Data-класс для параметров открытых (неперсонализированных) API-запросов
 * @param apiKey API-ключ для каждого запроса (по умолчанию вставляется из конфигурации)
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class SimpleInputData(
    val apiKey: String = BuildConfig.API_KEY
)

/**
 * Data-класс для параметров простых персонализированных API-запросов
 * @param session сессия пользователя
 * @param apiKey API-ключ для каждого запроса (по умолчанию вставляется из конфигурации)
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class SessionData(
    val session: String,
    val apiKey: String = BuildConfig.API_KEY
)

/** Общие настройки Json для кеша и API-запросов */
val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/** Http-клиент для осуществления API-запросов */
val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(json)
    }
}

/**
 * Singleton для осуществления всех API-запросов
 * @property V Глобальная версия API
 * @author Максим Дрючин (tgmaksim)
 * */
object Request {
    const val V = "v1"  // Версия API

    /**
     * Обобщенная функция для осуществления API-запросов с помощью метода GET с входными параметрами
     * @param path путь к нужному запросу
     * @param dataToSend объект [Serializable] data class с параметрами запроса
     * @return десериализованный результат запроса
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend inline fun <reified TIN : Any, reified TOUT : Any> get(
        path: String,
        dataToSend: TIN
    ): TOUT =
        httpClient.get(listOf(BuildConfig.DOMAIN, V, path).joinToString("/")) {
            contentType(ContentType.Application.Json)
            setBody(dataToSend, typeInfo<TIN>())
        }.body(typeInfo<TOUT>())

    /**
     * Обобщенная функция для осуществления API-запросов с помощью метода GET без входных параметров
     * @param path путь к нужному запросу
     * @return десериализованный результат запроса
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend inline fun <reified TOUT : Any> get(
        path: String
    ): TOUT =
        httpClient.get(listOf(BuildConfig.DOMAIN, V, path).joinToString("/")) {
            contentType(ContentType.Application.Json)
        }.body(typeInfo<TOUT>())

    /**
     * Обобщенная функция для осуществления API-запросов с помощью метода POST с входными параметрами
     * @param path путь к нужному запросу
     * @param dataToSend объект [Serializable] data class с параметрами запроса
     * @return десериализованный результат запроса
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend inline fun <reified TIN : Any, reified TOUT : Any> post(
        path: String,
        dataToSend: TIN
    ): TOUT =
        httpClient.post(listOf(BuildConfig.DOMAIN, V, path).joinToString("/")) {
            contentType(ContentType.Application.Json)
            setBody(dataToSend, typeInfo<TIN>())
        }.body(typeInfo<TOUT>())

    suspend fun checkInternet(): Boolean =
        try {
            httpClient.get(BuildConfig.DOMAIN)
            true
        } catch (e: Exception) {
            e !is CancellationException
        }
}