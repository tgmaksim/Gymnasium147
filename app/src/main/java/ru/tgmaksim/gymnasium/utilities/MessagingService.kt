package ru.tgmaksim.gymnasium.utilities

import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        Utilities.log("${message.notification?.title}\n${message.notification?.body}")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CacheManager.firebaseMessagingToken = token
    }
}