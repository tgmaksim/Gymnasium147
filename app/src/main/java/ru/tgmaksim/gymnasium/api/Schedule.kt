package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.BuildConfig
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.utilities.Utilities

/** Data-класс-результат запроса на получение расписания пользователя */
@Serializable
data class ScheduleData(
    val status: Boolean,
    val unauthorized: Boolean,
    val error: Boolean,
    val schedule: List<ScheduleDay>
)

/** Data-класс с расписанием на определенный день */
@Serializable
data class ScheduleDay(
    val date: String,
    val lessons: List<Lesson>,
    val hoursExtracurricularActivities: Hours?,
    val extracurricularActivities: List<ExtracurricularActivity>
) {
    val ea: List<ExtracurricularActivity>
        get() = extracurricularActivities

    val hoursEA: Hours?
        get() = hoursExtracurricularActivities
}

/** Data-класс с временем урока или внеурочки */
@Serializable
data class Hours(
    val start: String,
    val end: String
) {
    fun format(): String = "$start - $end"
}

/** Data-класс с данными об уроке */
@Serializable
data class Lesson(
    val number: Int,
    val subject: String,
    val place: String,
    val hours: Hours,
    val homework: String? = null,
    val files: List<HomeworkDocument> = emptyList()
)

/** Data-класс с данными о внеурочке */
@Serializable
data class ExtracurricularActivity(
    val subject: String,
    val place: String
)

/** Data-класс с данными о прикрепленном файле в домашнему заданию */
@Serializable
data class HomeworkDocument(
    val fileName: String,
    val downloadUrl: String
)

object Schedule {
    const val PATH_PREFIX = "dnevnik"
    const val PATH_GET_SCHEDULE = "getSchedule"

    /** Получение расписания на две недели (15 дней) */
    suspend fun getSchedule() : ScheduleData {
        val result = Request.get<SessionData, ScheduleData> (
            "$PATH_PREFIX/$PATH_GET_SCHEDULE",
            SessionData(
                BuildConfig.API_KEY,
                CacheManager.apiSession.toString()
            )
        )

        if (result.status)
            CacheManager.schedule = json.encodeToString(result.schedule)

        return result
    }

    /** Загрузка расписания из кеша, если ранее оно было сохранено */
    fun getCacheSchedule() : List<ScheduleDay> {
        return CacheManager.schedule?.let {
            try {
                return@let json.decodeFromString<List<ScheduleDay>>(CacheManager.schedule!!)
            } catch (e: Exception) {
                Utilities.log(e)
                CacheManager.schedule = null
                return@let emptyList()
            }
        } ?: emptyList()
    }
}