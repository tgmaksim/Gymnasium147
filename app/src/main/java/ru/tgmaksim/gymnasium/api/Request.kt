package ru.tgmaksim.gymnasium.api

import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.engine.cio.*
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.*

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

@Serializable
data class SimpleInputData(val apiKey: String)

@Serializable
data class SessionData(val apiKey: String, val session: String)

object Request {
    suspend inline fun <reified TIN : Any, reified TOUT : Any> get(
        path: String,
        dataToSend: TIN
    ): TOUT {
        val response: TOUT = httpClient.get("${Constants.DOMAIN}/$path") {
            contentType(ContentType.Application.Json)
            setBody(dataToSend, typeInfo<TIN>())
        }.body(typeInfo<TOUT>())

        return response
    }

    suspend inline fun <reified TIN : Any, reified TOUT : Any> post(
        path: String,
        dataToSend: TIN
    ): TOUT {
        val response: TOUT = httpClient.post("${Constants.DOMAIN}/$path") {
            contentType(ContentType.Application.Json)
            setBody(dataToSend, typeInfo<TIN>())
        }.body(typeInfo<TOUT>())

        return response
    }
}
