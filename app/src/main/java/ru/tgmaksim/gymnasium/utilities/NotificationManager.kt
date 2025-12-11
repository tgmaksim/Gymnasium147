package ru.tgmaksim.gymnasium.utilities

import android.Manifest
import android.os.Build
import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import android.content.Context
import android.app.AlarmManager
import android.provider.Settings
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import android.content.Context.NOTIFICATION_SERVICE

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.ui.MainActivity

object NotificationManager {
    const val CHANNEL_EA = "extracurricularActivities"
    const val ALARM_REQUEST_CODE_EA = 1

    /** Проверка разрешения на отправку уведомлений и запрос в случае необходимости */
    fun setupPostNotifications(activity: Activity) {
        // Создание канала уведомлений
        val notificationManager = activity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val exists = notificationManager.getNotificationChannel(CHANNEL_EA)

        if (exists == null) {
            val channelName = "Внеурочные занятия"
            val channelDescription = "Уведомления о том, что через несколько минут начнется внеурочное занятие"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_EA, channelName, importance)
            channel.description = channelDescription

            notificationManager.createNotificationChannel(channel)
        }

        // Явный запрос на уведомления для SDK >= 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = activity.getSystemService(AlarmManager::class.java)

            // TODO: учитывать разрешение в других местах
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = "package:${activity.packageName}".toUri()
                activity.startActivity(intent)
            }
        }
    }

    /** Проверка разрешения на отправку уведомлений */
    fun checkPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    /** Показывает уведомление */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(
        context: Context,
        channel: String,
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle())

        val manager = NotificationManagerCompat.from(context)
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    /** Планирует уведомление на время */
    fun addAlarmNotification(
        context: Context,
        channel: String,
        title: String,
        message: String,
        priority: Int,
        timestamp: Long,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", "notification")
            putExtra("channel", channel)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("priority", priority)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timestamp,
            pendingIntent
        )
    }
}