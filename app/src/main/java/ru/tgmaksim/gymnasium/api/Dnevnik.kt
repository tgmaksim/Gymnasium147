package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Data-класс для результата API-запроса /[Dnevnik.PATH_PREFIX]/[Dnevnik.PATH_GET_SCHEDULE]/.
 * @param status Статус запроса
 * @param unauthorized Сессия не авторизована
 * @param error Произошла ошибка на сервере
 * @param schedule Расписание на 2 недели (15 дней) в виде списка из расписания на каждый день [ScheduleDay]
 * @author Максим Дрючин (tgmaksim)
 * @see Dnevnik.getSchedule
 * @see Dnevnik.getCacheSchedule
 * */
@Serializable data class Schedule(
    val status: Boolean,
    val unauthorized: Boolean,
    val error: Boolean,
    val schedule: List<ScheduleDay>
)

/**
 * Data-класс с расписанием на определенный день.
 * @param date Дата дня в формате [ScheduleDay.DATE_FORMAT]
 * @param lessons Уроки в данный день в виде списка из уроков [Lesson]
 * @param hoursExtracurricularActivities Часы проведения внеурочек (если есть в данный день) в виде [Hours]
 * @param extracurricularActivities Внеурочки в данный день (если есть) в виде списка внеурочек [ExtracurricularActivity]
 * @property ea Краткое название для [extracurricularActivities]
 * @property hoursEA Краткое название для [hoursExtracurricularActivities]
 * @author Максим Дрючин (tgmaksim)
 * @see Schedule
 * */
@Serializable data class ScheduleDay(
    val date: String,
    val lessons: List<Lesson>,
    val hoursExtracurricularActivities: Hours?,
    val extracurricularActivities: List<ExtracurricularActivity>
) {
    val ea = extracurricularActivities
    val hoursEA = hoursExtracurricularActivities

    companion object {
        const val DATE_FORMAT = "yyyy-MM-dd"
    }
}

/**
 * Data-класс с временем урока или внеурочки.
 * @param start Начала урока или внеурочки
 * @param end Окончание урока или внеурочки
 * @author Максим Дрючин (tgmaksim)
 * @see Lesson
 * @see ExtracurricularActivity
 * */
@Serializable data class Hours(
    val start: String,
    val end: String
) {
    fun format(): String = "$start - $end"
}

/**
 * Data-класс с данными об уроке.
 * @param number Порядковый номер урока начиная с 0
 * @param subject Название предмета урока
 * @param place Кабинет или другое место проведения урока
 * @param hours Время проведения урока в виде [Hours]
 * @param homework Домашнее задание к уроку
 * @param files Дополнительные файлы к домашнему заданию в виде списка из файлов [HomeworkDocument]
 * @author Максим Дрючин (tgmaksim)
 * @see ScheduleDay
 * */
@Serializable data class Lesson(
    val number: Int,
    val subject: String,
    val place: String,
    val hours: Hours,
    val homework: String? = null,
    val files: List<HomeworkDocument> = emptyList()
)

/**
 * Data-класс с данными об внеурочке.
 * @param subject Название предмета внеурочки
 * @param place Кабинет или другое место проведения внеурочки
 * @author Максим Дрючин (tgmaksim)
 * @see ScheduleDay
 * */
@Serializable data class ExtracurricularActivity(
    val subject: String,
    val place: String
)

/**
 * Data-класс с данными о дополнительном файле к домашнему заданию.
 * @param fileName Название файла
 * @param downloadUrl Ссылка для загрузки файла
 * @author Максим Дрючин (tgmaksim)
 * @see Lesson
 * */
@Serializable data class HomeworkDocument(
    val fileName: String,
    val downloadUrl: String
)

/**
 * API-singleton для взаимодействия с дневником пользователя
 * @property PATH_PREFIX Группа API-запросов
 * @property PATH_GET_SCHEDULE Название API-запроса для получения расписания
 * @author Максим Дрючин (tgmaksim)
 * */
object Dnevnik {
    const val PATH_PREFIX = "dnevnik"
    const val PATH_GET_SCHEDULE = "getSchedule"

    /**
     * Получение актуального расписания запросом на сервер.
     * @return расписание на 2 недели (15 дней) в виде [Schedule]
     * @author Максим Дрючин (tgmaksim)
     * */
    suspend fun getSchedule() : Schedule {
        val result = Request.get<SessionData, Schedule> (
            listOf(PATH_PREFIX, PATH_GET_SCHEDULE).joinToString("/"),
            SessionData(CacheManager.apiSession.toString())
        )

        if (result.status)
            CacheManager.schedule = json.encodeToString(result.schedule)

        return result
    }

    /**
     * Получение сохраненного расписания из кеша.
     * @return расписание на 2 недели (15 дней) в виде списка из [ScheduleDay]
     * @author Максим Дрючин (tgmaksim)
     * */
    fun getCacheSchedule() : List<ScheduleDay> =
        CacheManager.schedule?.let {
            try {
                return json.decodeFromString<List<ScheduleDay>>(it)
            } catch (e: Exception) {
                // При возникновении ошибки десериализации расписание в кеше очищается
                Utilities.log(e)
                CacheManager.schedule = null
                return emptyList()
            }
        } ?: emptyList()
}