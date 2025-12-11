package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.Serializable

import java.io.EOFException
import java.net.SocketException
import java.net.ConnectException
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.net.NoRouteToHostException
import java.net.PortUnreachableException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException as KtorSocketTimeoutException

import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import java.security.cert.CertificateExpiredException
import java.security.InvalidAlgorithmParameterException
import java.security.cert.CertificateNotYetValidException

import ru.tgmaksim.gymnasium.utilities.Utilities

/** Data-класс с данными о текущей версии приложения */
@Serializable
data class Versions(
    val minApiVersion: Int? = null,
    val latestVersion: Int? = null,
    val api: Boolean? = null,  // Если версия API устарела, то api=false
    val updateLog: String? = null
)

@Serializable
data class VersionStatus(
    /** Глобальная версия API больше не работает */
    val apiVersionDeprecated: Boolean = false,

    /** На сервере обновление: некоторые запросы могут не работать */
    val newApiVersion: Boolean = false,

    /** Вышла новая версия приложения */
    val newLatestVersion: Boolean = false,

    /** Список новых функций в обновлении */
    val updateLog: String? = null
)

object VersionChecker {
    private const val PATH_CHECK_VERSION = "checkVersion"

    /** Проверка текущей версии приложения */
    suspend fun checkVersion(version: Int): VersionStatus {
        try {
            val response = Request.get<Versions>(PATH_CHECK_VERSION)

            if (response.api == false)
                return VersionStatus(
                    apiVersionDeprecated = true,
                    newApiVersion = true,
                    newLatestVersion = true
                )

            if (response.minApiVersion!! > version)
                return VersionStatus(
                    newApiVersion = true,
                    newLatestVersion = true,
                    updateLog = response.updateLog
                )

            if (response.latestVersion!! > version)
                return VersionStatus(
                    newLatestVersion = true,
                    updateLog = response.updateLog ?: "• Мелкие исправления и улучшения"
                )

        } catch (e: NullPointerException) {
            Utilities.log(e)
            return VersionStatus(
                apiVersionDeprecated = true,
                newApiVersion = true,
                newLatestVersion = true
            )
        }

        // Ошибки, не связанные с ответом сервера, воспринимаются как обычная версия
        catch (_: ConnectTimeoutException) {}
        catch (_: KtorSocketTimeoutException) {}
        catch (_: ConnectException) {}
        catch (_: SocketTimeoutException) {}
        catch (_: NoRouteToHostException) {}
        catch (_: PortUnreachableException) {}
        catch (_: UnknownHostException) {}
        catch (_: EOFException) {}
        catch (_: SocketException) {}
        catch (_: SSLHandshakeException) {}
        catch (_: SSLPeerUnverifiedException) {}
        catch (_: SSLException) {}
        catch (_: CertificateExpiredException) {}
        catch (_: CertificateNotYetValidException) {}
        catch (_: InvalidAlgorithmParameterException) {}

        catch (e: Exception) {
            Utilities.log(e)
            return VersionStatus(
                apiVersionDeprecated = true,
                newApiVersion = true,
                newLatestVersion = true
            )
        }

        return VersionStatus()
    }
}