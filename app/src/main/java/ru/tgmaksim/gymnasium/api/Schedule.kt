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
    val homework: String?
)

@Serializable
data class ExtracurricularActivity(
    val subject: String,
    val place: String
)

object Schedule {
    const val PATH_GET_SCHEDULE = "getSchedule"

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

    fun getCacheSchedule() : List<ScheduleDay> {
        if (CacheManager.schedule != null)
            return Json.decodeFromString<List<ScheduleDay>>(CacheManager.schedule!!)

        return emptyList()
    }
}