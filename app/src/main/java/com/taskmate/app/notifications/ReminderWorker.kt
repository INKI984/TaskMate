package com.taskmate.app.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.taskmate.app.R

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE)
            ?: applicationContext.getString(R.string.reminder_notification_title)
        val notifId = inputData.getInt(KEY_NOTIF_ID, 1)
        val channelId = applicationContext.getString(R.string.default_notification_channel_id)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.reminder_notification_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(notifId, notification)
        } catch (e: SecurityException) {
            // Нема дозвола за нотификации — игнорирај, без пад.
        }
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_NOTIF_ID = "notif_id"
    }
}