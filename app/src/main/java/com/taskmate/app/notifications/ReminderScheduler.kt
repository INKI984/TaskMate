package com.taskmate.app.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.taskmate.app.data.local.Task
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private fun workName(taskId: String) = "reminder_$taskId"

    fun schedule(context: Context, task: Task) {
        val due = task.dueDate
        val now = System.currentTimeMillis()

        if (task.isCompleted || due == null || due <= now) {
            cancel(context, task.id)
            return
        }

        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, task.title)
            .putInt(ReminderWorker.KEY_NOTIF_ID, task.id.hashCode())
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(due - now, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(task.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }
}