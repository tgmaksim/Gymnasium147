package ru.tgmaksim.gymnasium.utilities

import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        message.notification?.let {
            Utilities.log("message: ${it.title}\n${it.body}", tag="notifications") {
                param("type", "api_send")
                param("channel", it.channelId.toString())
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CacheManager.firebaseMessagingToken = token
        Utilities.log("Новый FirebaseMessagingToken: $token", tag="notifications") {
            param("type", "new_token")
        }
    }
}