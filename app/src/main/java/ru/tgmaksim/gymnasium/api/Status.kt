package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.Serializable

/**
 * Запрос данных о последней версии приложения
 * @param classId Идентификатор класса
 * @param data Пустые входные данные
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class VersionsApiRequest(
    override val classId: Int = CLASS_ID,
    override val data: ApiBase? = null
) : ApiRequest() {
    companion object {
        const val CLASS_ID = 0x00000004
    }
}

/**
 * Результат запроса данных о последней версии приложения
 * @param classId Идентификатор класса
 * @param latestVersionNumber Последняя доступная версия (номер сборки) приложения
 * @param latestVersionString Последняя доступная версия приложения
 * @param date Дата выпуска последней доступной версии приложения
 * @param versionStatus Статус новой версии, означающий важность обновления
 * @param updateLogs Изменения в последней версии приложения (latestVersion), которые можно показать пользователю
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class VersionsResult(
    override val classId: Int = CLASS_ID,
    val latestVersionNumber: Int,
    val latestVersionString: String,
    val date: String,
    val versionStatus: String,
    val updateLogs: String
) : ApiBase() {
    companion object {
        const val CLASS_ID = 0x00000005
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * Ответ на запрос данных о последней версии приложения
 * @param classId Идентификатор класса
 * @param status Статус выполненного запроса
 * @param error Объект ошибки
 * @param answer Ответ в случае успешной обработки
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class VersionsApiResponse(
    override val classId: Int = CLASS_ID,
    override val status: Boolean,
    override val error: ApiError?,
    override val answer: VersionsResult?
) : ApiResponse() {
    companion object {
        const val CLASS_ID = 0x00000006
    }
    init {
        if (classId != CLASS_ID && classId != ApiResponse.CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * API-singleton для запросов группы status
 * @property PATH_STATUS Название группы API-запросов
 * @property PATH_CHECK_VERSION Название API-запроса для проверки версии
 * @author Максим Дрючин (tgmaksim)
 * */
object Status {
    private const val PATH_STATUS = "status"
    private const val PATH_CHECK_VERSION = "checkVersion"

    /**
     * Получение данных о последней доступной версии приложения
     * @return Ответ сервера в виде [VersionsApiResponse]
     * @exception Exception
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend fun checkVersion(): VersionsApiResponse {
        val request = VersionsApiRequest()

        val response = Request.post<VersionsApiRequest, VersionsApiResponse>(
            listOf(PATH_STATUS, PATH_CHECK_VERSION, VersionsApiRequest.CLASS_ID).joinToString("/"),
            request
        )

        return response
    }
}