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

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Лишние параметры игнорируются
        })
    }
}

@Serializable
data class SimpleInputData(val apiKey: String)

@Serializable
data class SessionData(val apiKey: String, val session: String)

object Request {
    /** Общая функция для совершения API-запросов с помощью метода GET */
    suspend inline fun <reified TIN : Any, reified TOUT : Any> get(
        path: String,
        dataToSend: TIN
    ): TOUT {
        return httpClient.get("${Constants.DOMAIN}/$path") {
            contentType(ContentType.Application.Json)
            setBody(dataToSend, typeInfo<TIN>())
        }.body(typeInfo<TOUT>())
    }

    /** Общая функция для совершения API-запросов с помощью метода POST */
    suspend inline fun <reified TIN : Any, reified TOUT : Any> post(
        path: String,
        dataToSend: TIN
    ): TOUT {
        return httpClient.post("${Constants.DOMAIN}/$path") {
            contentType(ContentType.Application.Json)
            setBody(dataToSend, typeInfo<TIN>())
        }.body(typeInfo<TOUT>())
    }
}