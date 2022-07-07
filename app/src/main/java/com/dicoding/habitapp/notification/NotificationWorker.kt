package com.dicoding.habitapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.data.HabitDatabase
import com.dicoding.habitapp.data.HabitRepository
import com.dicoding.habitapp.ui.detail.DetailHabitActivity
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID
import java.util.concurrent.Executors

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val habitId = inputData.getInt(HABIT_ID, 0)
    private val habitTitle = inputData.getString(HABIT_TITLE)
    private val channelName = inputData.getString(NOTIFICATION_CHANNEL_ID)

    override fun doWork(): Result {
        val prefManager =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val shouldNotify =
            prefManager.getBoolean(applicationContext.getString(R.string.pref_key_notify), false)

        //TODO 12 : If notification preference on, show notification with pending intent

        when (shouldNotify) {
            true -> {
                val notificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification: NotificationCompat.Builder =
                    NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setContentTitle(habitTitle)
                        .setContentIntent(getPendingIntent())
                        .setContentText(

                            applicationContext.getString(R.string.notify_content),

                            )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notification.setChannelId(NOTIFICATION_CHANNEL_ID)
                    notificationManager.createNotificationChannel(channel)
                }
                notificationManager.notify(1, notification.build())
                return Result.success()
            }


            else -> {
                return Result.failure()
            }
        }
    }

    private fun getPendingIntent()
            : PendingIntent
    ? {
        val intent = Intent(applicationContext, DetailHabitActivity::class.java).apply {
            putExtra(HABIT_ID, habitId)
        }
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}