package ru.tgmaksim.gymnasium.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.serialization.Serializable

import java.net.URL
import java.io.File
import java.io.IOException
import kotlinx.coroutines.delay
import java.net.HttpURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
import ru.tgmaksim.gymnasium.utilities.CacheManager

@Serializable
data class Versions(
    val minApiVersion: Int? = null,
    val latestVersion: Int? = null,
    val api: Boolean? = null,
    val updateLog: String? = null,
    val sha256: String? = null
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
    val updateLog: String? = null,

    /** Хеш-сумма файла */
    val sha256: String? = null
)

object VersionChecker {
    private const val PATH_CHECK_VERSION = "checkVersion"
    private const val PATH_LOAD_UPDATE = "app-release.apk"

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
                    updateLog = response.updateLog,
                    sha256 = response.sha256
                )

            if (response.latestVersion!! > version)
                return VersionStatus(
                    newLatestVersion = true,
                    updateLog = response.updateLog ?: "• Мелкие исправления и улучшения",
                    sha256 = response.sha256
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

        catch (_: Exception) {
            return VersionStatus(
                apiVersionDeprecated = true,
                newApiVersion = true,
                newLatestVersion = true
            )
        }

        return VersionStatus()
    }

    /** Загрузка обновления в кеш */
    suspend fun loadUpdate(
        context: Context,
        onProgress: (percent: Int) -> Unit,
        maxRetries: Int = 3
    ): Boolean {
        val fileName = PATH_LOAD_UPDATE.substringAfterLast("/")
        val file = File(context.cacheDir, fileName)

        // Проверка уже скачанного файла на идентичность
        val sha256 = if (file.exists()) Utilities.getFileSha256(file) else null
        if (CacheManager.versionStatus.sha256 == sha256) {
            onProgress(100)
            return true
        }

        // Скачивание файла
        return loadApk(file, onProgress, maxRetries)
    }

    /** Скачивание файла с обновлением в файл [file] */
    private suspend fun loadApk(
        file: File,
        onProgress: (percent: Int) -> Unit,
        maxRetries: Int = 3
    ): Boolean = withContext(Dispatchers.IO) {
        val url = "${Constants.DOMAIN}/${PATH_LOAD_UPDATE}"

        // Несколько попыток скачивания
        var attempt = 0
        while (attempt < maxRetries) {
            try {
                // Удаление старого файла
                if (file.exists())
                    file.delete()

                // Инициализация соединения
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.requestMethod = "GET"
                connection.doInput = true
                connection.connect()

                // Сервер не вернул файл
                if (connection.responseCode != HttpURLConnection.HTTP_OK)
                    throw IOException("Server returned HTTP ${connection.responseCode}")

                val totalSize = connection.contentLength
                var downloadedSize = 0L

                // Скачивание файла в память
                connection.inputStream.use { input ->
                    file.outputStream().use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedSize += bytesRead

                            if (totalSize > 0) {
                                val percent = (downloadedSize * 100 / totalSize).toInt()
                                onProgress(percent)
                            } else {
                                onProgress(100)
                            }
                        }
                    }
                }

                // Успешно скачано
                return@withContext true

            } catch (e: Exception) {
                Utilities.log(e)
                attempt++
                onProgress(0)

                if (attempt >= maxRetries)
                    return@withContext false

                // Ожидание между попытками
                delay(1000)
            }
        }

        false
    }

    /** Установка обновления */
    fun installApk(activity: Activity) {
        val fileName = PATH_LOAD_UPDATE.substringAfterLast("/")
        val file = File(activity.cacheDir, fileName)

        // Получение Uri файла в папке кеша приложения
        val apkUri = FileProvider.getUriForFile(
            activity,
            activity.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")

        activity.startActivityForResult(intent, 500)
    }

    /** Проверяет разрешение на установку Apk */
    fun checkPermissionApk(activity: Activity): Boolean {
        return activity.packageManager.canRequestPackageInstalls()
    }

    /** При необходимости запрашивает разрешение на установку Apk */
    fun requestPermissionApk(activity: Activity) {
        if (checkPermissionApk(activity))
            return

        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = "package:${activity.packageName}".toUri()
        }

        activity.startActivityForResult(intent, 1234)
    }
}