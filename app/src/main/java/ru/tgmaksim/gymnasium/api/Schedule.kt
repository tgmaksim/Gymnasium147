package ru.tgmaksim.gymnasium.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.CacheManager

@Serializable
data class Schedules(
    val status: Boolean,
    val unauthorized: Boolean,
    val error: Boolean,
    val schedule: List<ScheduleDay>
)

@Serializable
data class ScheduleDay(
    val date: String,
    val lessons: List<Lesson>,
    val hoursExtracurricularActivities: String?,
    val extracurricularActivities: List<ExtracurricularActivity>
)

@Serializable
data class Lesson(
    val number: Int,
    val subject: String,
    val place: String,
    val hours: String,
    val homework: String? = null,
    val files: List<HomeworkDocument> = emptyList()
)

@Serializable
data class ExtracurricularActivity(
    val subject: String,
    val place: String
)

@Serializable
data class HomeworkDocument(
    val fileName: String,
    val downloadUrl: String
)

object Schedule {
    const val PATH_GET_SCHEDULE = "getSchedule"

    /** Получает расписания на две недели (15 дней) */
    suspend fun getSchedule() : Schedules {
        val result: Schedules = Request.get<SessionData, Schedules> (
            PATH_GET_SCHEDULE,
            SessionData(
                Constants.API_KEY,
                CacheManager.apiSession.toString()
            )
        )

        if (result.status)
            CacheManager.schedule = Json.encodeToString(result.schedule)

        return result
    }

    /** Загружает расписание из кеша, если ранее оно было сохранено */
    fun getCacheSchedule() : List<ScheduleDay> {
        val schedule = CacheManager.schedule
        if (schedule != null) {
            // Если формат расписания был изменен возникнет ошибка
            try {
                return Json.decodeFromString<List<ScheduleDay>>(schedule)
            } catch (_: Exception) {
                CacheManager.schedule = null
            }
        }

        return emptyList()
    }
}