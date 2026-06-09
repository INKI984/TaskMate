package com.taskmate.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.taskmate.app.data.local.AppDatabase
import com.taskmate.app.data.repository.TaskRepository
import com.taskmate.app.util.Constants

/**
 * Application класа — едноставен service locator за репозиториумот
 * и место каде се креира нотификацискиот канал.
 */
class TaskMateApp : Application() {

    // Lazy иницијализација на Room базата и репозиториумот.
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: TaskRepository by lazy { TaskRepository(this, database.taskDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Стандарден јазик: македонски (ако корисникот сè уште нема избрано)
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(Constants.LANG_MK)
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}