package ru.tgmaksim.gymnasium.api

import android.content.Context
import kotlinx.serialization.Serializable

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager

@Serializable
data class LoginUrl(val loginUrl: String, val session: String)

@Serializable
data class SessionStatus(val exists: Boolean, val auth: Boolean)

object Login {
    const val PATH_LOGIN: String = "login"
    const val PATH_CHECK_SESSION = "checkSession"

    private suspend fun getLoginUrl(): String {
        val loginUrl: LoginUrl = Request.post<SimpleInputData, LoginUrl>(
            PATH_LOGIN,
            SimpleInputData(Constants.API_KEY)
        )
        CacheManager.apiSession = loginUrl.session
        return loginUrl.loginUrl
    }

    suspend fun login(context: Context) {
        val url: String = getLoginUrl()
        Utilities.openUrl(context, url)
    }

    suspend fun checkSession(): SessionStatus {
        if (CacheManager.apiSession?.isEmpty() == true)
            return SessionStatus(exists = false, auth = false)
        val session: String = CacheManager.apiSession.toString()

        return Request.post<SessionData, SessionStatus>(
            PATH_CHECK_SESSION,
            SessionData(Constants.API_KEY, session)
        )
    }
}