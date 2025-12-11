package ru.tgmaksim.gymnasium.utilities

import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type")

        if (!NotificationManager.checkPermission(context)) {
            CacheManager.EANotifications = false
            return
        }

        if (type == "notification") {
            val channel = intent.getStringExtra("channel")
            val title = intent.getStringExtra("title")
            val message = intent.getStringExtra("message")
            val priority = intent.getIntExtra("priority", -1)

            if (channel == NotificationManager.CHANNEL_EA && !CacheManager.EANotifications)
                return

            if (channel != null && title != null && message != null && priority != -1) {
                NotificationManager.showNotification(
                    context,
                    channel,
                    title,
                    message,
                    priority
                )
            }
        }
    }
}