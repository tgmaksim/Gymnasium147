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

/**
 * Data-класс для низкоуровневого результата API-запроса /[VersionChecker.PATH_CHECK_VERSION]/.
 * @param minApiVersion Минимальная версия API для корректной работы
 * @param latestVersion Последняя версия приложения
 * @param api Если глобальная версия API устарела, то false, иначе true
 * @param updateLog Описание обновления приложения
 * @author Максим Дрючин (tgmaksim)
 * @see VersionChecker.checkVersion
 * */
@Serializable private data class Versions(
    val minApiVersion: Int? = null,
    val latestVersion: Int? = null,
    val api: Boolean? = null,  // Если версия API устарела, то api=false
    val updateLog: String? = null
)

/**
 * Data-класс для результата API-запроса /[VersionChecker.PATH_CHECK_VERSION]/.
 * @param apiVersionDeprecated Глобальная версия API больше не поддерживается
 * @param newApiVersion На сервере обновление: некоторые запросы могут не работать
 * @param newLatestVersion Вышла новая версия приложения
 * @param updateLog Описание обновления приложения
 * @author Максим Дрючин (tgmaksim)
 * @see VersionChecker.checkVersion
 * */
@Serializable data class VersionStatus(
    val apiVersionDeprecated: Boolean = false,
    val newApiVersion: Boolean = false,
    val newLatestVersion: Boolean = false,
    val updateLog: String? = null
)

/**
 * API-singleton для проверки версии приложения
 * @property PATH_CHECK_VERSION Название API-запроса для проверки версии
 * @author Максим Дрючин (tgmaksim)
 * */
object VersionChecker {
    private const val PATH_CHECK_VERSION = "checkVersion"

    /**
     * Проверка версии приложения.
     * @param version текущий номер сборки приложения
     * @return статус текущей версии приложения в виде [VersionStatus]
     * @author Максим Дрючин (tgmaksim)
     * */
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
            if (Request.checkInternet())
                return VersionStatus(
                    apiVersionDeprecated = true,
                    newApiVersion = true,
                    newLatestVersion = true
                )
        }

        return VersionStatus()
    }
}