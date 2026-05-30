package com.rise.collegealert

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        val title =
            inputData.getString("title")
                ?: "College Alert"

        val message =
            inputData.getString("message")
                ?: "Upcoming Event Reminder"

        val channelId = "college_alert_channel"

        val soundUri =
            RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "College Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.description =
                "Event Reminder Notifications"

            channel.enableVibration(true)

            channel.setSound(
                soundUri,
                null
            )

            val manager =
                applicationContext.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(channel)
        }

        val builder =
            NotificationCompat.Builder(
                applicationContext,
                channelId
            )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return Result.failure()
            }
        }

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                System.currentTimeMillis().toInt(),
                builder.build()
            )

        return Result.success()
    }
}