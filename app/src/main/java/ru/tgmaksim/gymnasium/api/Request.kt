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

/** Data-класс для открытых запросов без привязки к пользователю */
@Serializable
data class SimpleInputData(
    val apiKey: String
)

/** Data-класс для простых запросов от имени пользователя */
@Serializable
data class SessionData(
    val apiKey: String,
    val session: String
)

/** Общие настройки Json */
val json = Json {
    ignoreUnknownKeys = true
}

/** Http-клиент для осуществления API-запросов */
val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(json)
    }
}

object Request {
    const val V = "v1"  // Версия API

    /** API-запрос с помощью метода GET на получение данных с входными параметрами */
    suspend inline fun <reified TIN : Any, reified TOUT : Any> get(
        path: String,
        dataToSend: TIN
    ): TOUT =
        httpClient.get("${BuildConfig.DOMAIN}/$V/$path") {
            contentType(ContentType.Application.Json)
            setBody(dataToSend, typeInfo<TIN>())
        }.body(typeInfo<TOUT>())

    /** API-запрос с помощью метода GET на получение данных без входных параметров */
    suspend inline fun <reified TOUT : Any> get(path: String): TOUT =
        httpClient.get("${BuildConfig.DOMAIN}/$V/$path") {
            contentType(ContentType.Application.Json)
        }.body(typeInfo<TOUT>())

    /** API-запрос с помощью метода POST на осуществление операций с входными данными */
    suspend inline fun <reified TIN : Any, reified TOUT : Any> post(
        path: String,
        dataToSend: TIN
    ): TOUT =
        httpClient.post("${BuildConfig.DOMAIN}/$V/$path") {
            contentType(ContentType.Application.Json)
            setBody(dataToSend, typeInfo<TIN>())
        }.body(typeInfo<TOUT>())
}