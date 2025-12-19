package ru.tgmaksim.gymnasium.utilities

import android.content.Intent
import android.content.Context
import java.time.LocalDateTime
import java.time.ZonedDateTime
import android.content.BroadcastReceiver
import androidx.core.app.NotificationCompat

import ru.tgmaksim.gymnasium.api.ScheduleExtracurricularActivity

/**
 * Receiver для уведомлений-напоминаний
 * @author Максим Дрючин (tgmaksim)
 * */
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val MAX_TIMEOUT = 15 * 60 * 1000  // 15 минут в миллисекундах

        /**
         * Создание напоминания о внеурочном занятии
         * @param context Android-context для создания напоминания
         * @param ea Список внеурочных занятий, к=о которых необходимо напомнить
         * @param eaStart Дата и время начала внеурочного занятия
         * @author Максим Дрючин (tgmaksim)
         * */
        fun createRemindEA(context: Context, ea: List<ScheduleExtracurricularActivity>, eaStart: LocalDateTime) {
            val timestamp = eaStart
                .minusMinutes(15)  // За 15 минут до начала
                .toInstant(ZonedDateTime.now().offset)
                .toEpochMilli()

            NotificationManager.addAlarmNotification(
                context,
                NotificationManager.CHANNEL_EA,
                "Внеурочное занятие",
                "Напоминаю! Через 15 минут начнется ${ea.joinToString { it.subject }}",
                NotificationCompat.PRIORITY_HIGH,
                timestamp,
                NotificationManager.ALARM_REQUEST_CODE_EA
            )

            Utilities.log("Создано напоминание на $timestamp")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type")

        if (!NotificationManager.checkPermission(context)) {
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
                Utilities.log("Отправлено уведомление $title: $message")
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