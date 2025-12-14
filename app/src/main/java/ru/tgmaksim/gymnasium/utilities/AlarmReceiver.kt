package ru.tgmaksim.gymnasium.utilities

import android.content.Intent
import android.content.Context
import java.time.LocalDateTime
import java.time.ZonedDateTime
import android.content.BroadcastReceiver

/**
 * Receiver для уведомлений-напоминаний
 * @author Максим Дрючин (tgmaksim)
 * */
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val MAX_TIMEOUT = 15 * 60 * 1000  // 15 минут в миллисекундах
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type")

        if (!NotificationManager.checkPermission(context) || !NotificationManager.canScheduleExactAlarms(context)) {
            CacheManager.EANotifications = false
            return
        }

        if (type == "notification") {
            val timestamp = intent.getLongExtra("timestamp", -1)
            val channel = intent.getStringExtra("channel")
            val title = intent.getStringExtra("title")
            val message = intent.getStringExtra("message")
            val priority = intent.getIntExtra("priority", -1)

            if (channel == NotificationManager.CHANNEL_EA && !CacheManager.EANotifications)
                return

            val nowMilli = LocalDateTime.now().toInstant(ZonedDateTime.now().offset).toEpochMilli()
            if (timestamp != -1L && nowMilli - timestamp > MAX_TIMEOUT) {
                Utilities.log("Timeout notification: $timestamp")
                return
            }

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

        // TODO: добавить новое напоминание о внеурочке
    }
}