package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Запрос расписания на 2 недели (15 дней)
 * @param classId Идентификатор класса
 * @param data Данные сессии
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class ScheduleApiRequest(
    override val classId: Int = CLASS_ID,
    override val data: ApiSession?
) : ApiRequest() {
    companion object {
        const val CLASS_ID = 0x0000000D
    }
}

/**
 * Прикрепленный файл к домашнему заданию
 * @param fileName Название файла
 * @param downloadUrl Ссылка для загрузки файла
 * @author Максим Дрючин (tgmaksim)
 * @see ScheduleLesson
 * */
@Serializable data class ScheduleHomeworkDocument(
    override val classId: Int = CLASS_ID,
    val fileName: String,
    val downloadUrl: String
) : ApiBase() {
    companion object {
        const val CLASS_ID = 0x0000000E
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * Внеурочное занятие
 * @param subject Название предмета внеурочного занятия
 * @param place Кабинет или другое место проведения внеурочного занятия
 * @author Максим Дрючин (tgmaksim)
 * @see ScheduleDay
 * */
@Serializable data class ScheduleExtracurricularActivity(
    override val classId: Int = CLASS_ID,
    val subject: String,
    val place: String
) : ApiBase() {
    companion object {
        const val CLASS_ID = 0x0000000F
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * Время проведения урока или внеурочного занятия
 * @param start Начало урока или внеурочного занятия
 * @param end Окончание урока или внеурочного занятия
 * @author Максим Дрючин (tgmaksim)
 * @see ScheduleLesson
 * @see ScheduleExtracurricularActivity
 * */
@Serializable data class ScheduleHours(
    override val classId: Int = CLASS_ID,
    val start: String,
    val end: String
) : ApiBase() {
    companion object {
        const val CLASS_ID = 0x00000010
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }

    val stringFormat: String
        get() = "$start - $end"
}

/**
 * Урок
 * @param number Порядковый номер урока начиная с 0
 * @param subject Название предмета урока
 * @param place Кабинет или другое место проведения урока
 * @param hours Время проведения урока в виде [ScheduleHours]
 * @param homework Домашнее задание к уроку
 * @param files Дополнительные файлы к домашнему заданию в виде списка из файлов [ScheduleHomeworkDocument]
 * @author Максим Дрючин (tgmaksim)
 * @see ScheduleDay
 * */
@Serializable data class ScheduleLesson(
    override val classId: Int = CLASS_ID,
    val number: Int,
    val subject: String,
    val place: String,
    val hours: ScheduleHours,
    val homework: String?,
    val files: List<ScheduleHomeworkDocument>
) : ApiBase() {
    companion object {
        const val CLASS_ID = 0x00000011
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * День в расписании с уроками и внеурочными занятиями
 * @param date Дата дня в формате [ScheduleDay.DATE_FORMAT]
 * @param lessons Уроки в данный день в виде списка из уроков [ScheduleLesson]
 * @param hoursExtracurricularActivities Часы проведения внеурочек (если есть в данный день) в виде [ScheduleHours]
 * @param extracurricularActivities Внеурочные занятия в данный день (если есть) в виде списка внеурочек [ScheduleExtracurricularActivity]
 * @property ea Краткое название для [extracurricularActivities]
 * @property hoursEA Краткое название для [hoursExtracurricularActivities]
 * @author Максим Дрючин (tgmaksim)
 * @see ScheduleResult
 * */
@Serializable data class ScheduleDay(
    override val classId: Int = CLASS_ID,
    val date: String,
    val lessons: List<ScheduleLesson>,
    val hoursExtracurricularActivities: ScheduleHours?,
    val extracurricularActivities: List<ScheduleExtracurricularActivity>
) : ApiBase() {
    companion object {
        const val CLASS_ID = 0x00000012
        const val DATE_FORMAT = "yyyy-MM-dd"
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }

    val ea = extracurricularActivities
    val hoursEA = hoursExtracurricularActivities
}

/**
 * Результат запроса расписания на 2 недели (15 дней)
 * @param classId Идентификатор класса
 * @param schedule Расписание на 2 недели (15 дней)
 * @author Максим Дрючин (tgmaksim)
 * */
@Serializable data class ScheduleResult(
    override val classId: Int = CLASS_ID,
    val schedule: List<ScheduleDay>
) : ApiBase() {
    companion object {
        const val CLASS_ID = 0x00000013
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * Ответ на запрос расписания на 2 недели (15 дней)
 * @property classId Идентификатор класса
 * @property status Статус выполненного запроса
 * @property error Объект API-ошибки
 * @property answer Ответ в случае успешной обработки
 * */
@Serializable data class ScheduleApiResponse(
    override val classId: Int,
    override val status: Boolean,
    override val error: ApiError?,
    override val answer: ScheduleResult?
) : ApiResponse() {
    companion object {
        const val CLASS_ID = 0x00000014
    }
    init {
        if (classId != CLASS_ID)
            throw ClassCastException()
    }
}

/**
 * API-singleton для взаимодействия с дневником пользователя
 * @property PATH_PREFIX Группа API-запросов
 * @property PATH_GET_SCHEDULE Название API-запроса для получения расписания
 * @author Максим Дрючин (tgmaksim)
 * */
object Dnevnik {
    private const val PATH_PREFIX = "dnevnik"
    private const val PATH_GET_SCHEDULE = "getSchedule"

    /**
     * Запрос расписания на 2 недели (15 дней)
     * @return Ответ сервера в виде [ScheduleApiResponse]
     * @exception Exception
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend fun getSchedule() : ScheduleApiResponse {
        val request = ScheduleApiRequest(data = ApiSession(session = CacheManager.apiSession.toString()))

        val response = Request.post<ScheduleApiRequest, ScheduleApiResponse> (
            listOf(PATH_PREFIX, PATH_GET_SCHEDULE, ScheduleApiRequest.CLASS_ID).joinToString("/"),
            request
        )

        // Сохранение расписания в кеш
        response.answer?.let {
            CacheManager.schedule = json.encodeToString(it.schedule)
        }

        return response
    }

    /**
     * Получение сохраненного расписания из кеша
     * @return Расписание на 2 недели (15 дней) в виде списка из [ScheduleDay]
     * @author Максим Дрючин (tgmaksim)
     * */
    fun getCacheSchedule() : List<ScheduleDay> {
        return CacheManager.schedule?.let {
            try {
                return@let json.decodeFromString<List<ScheduleDay>>(it)
            } catch (e: Exception) {
                // При возникновении ошибки десериализации расписание в кеше очищается
                Utilities.log(e)
                CacheManager.schedule = null
                return@let emptyList()
            }
        } ?: emptyList()
    }
}