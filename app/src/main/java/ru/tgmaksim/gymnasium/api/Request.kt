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
import io.ktor.serialization.kotlinx.json.json
import java.util.concurrent.CancellationException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

import ru.tgmaksim.gymnasium.BuildConfig

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
 * @author Максим Дрючин (tgmaksim)
 * */
object Request {
    /**
     * Обобщенная функция для осуществления API-запросов с помощью метода POST с входными параметрами
     * @param path Путь к нужному запросу
     * @param request Объект [TReq] с параметрами запроса
     * @return Десериализованный результат запроса в виде [TRes]
     * @exception Exception
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend inline fun <reified TReq : ApiRequest, reified TRes : ApiResponse> post(
        path: String,
        request: TReq
    ): TRes {
        return httpClient.post(listOf(BuildConfig.DOMAIN, path).joinToString("/")) {
            contentType(ContentType.Application.Json)
            setBody(request, typeInfo<TReq>())
        }.body(typeInfo<TRes>())
    }

    /**
     * Проверка соединения с интернетом путем попытки подключения к серверу API
     * @return Статус проверки
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend inline fun checkInternet(): Boolean =
        try {
            httpClient.get(BuildConfig.CHECK_INTERNET_DOMAIN)
            true
        } catch (e: Exception) {
            e is CancellationException
        }
}