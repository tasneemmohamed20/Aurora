package com.example.aurora.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.WorkManager
import com.example.aurora.MainActivity
import com.example.aurora.R
import kotlin.or

class WeatherAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WeatherAlertWorker.ACTION_DISMISS_ALERT -> {
                val alertId = intent.getStringExtra(WeatherAlertWorker.EXTRA_ALERT_ID)
                if (alertId != null) {
                    // Cancel notification
                    val notificationManager = context.getSystemService(NotificationManager::class.java)
                    notificationManager.cancel(alertId.hashCode())

                    // Cancel associated work
                    WorkManager.getInstance(context).cancelUniqueWork("weatherAlert_$alertId")
                }
            }
            WeatherAlertWorker.ACTION_SHOW_ALERT -> {
                val alertId = intent.getStringExtra(WeatherAlertWorker.EXTRA_ALERT_ID)
                val useDefaultSound = intent.getBooleanExtra("useDefaultSound", true)
                if (alertId != null) {
                    showWeatherAlert(context, alertId, useDefaultSound)
                }
            }
        }
    }

    private fun showWeatherAlert(context: Context, alertId: String, useDefaultSound: Boolean) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Create intent for notification click

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Add extra to indicate navigation to home
            putExtra("destination", "home")
        }

        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(contentIntent)
            getPendingIntent(
                alertId.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Create dismiss intent
        val dismissIntent = Intent(context, WeatherAlertReceiver::class.java).apply {
            action = WeatherAlertWorker.ACTION_DISMISS_ALERT
            putExtra(WeatherAlertWorker.EXTRA_ALERT_ID, alertId)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alertId.hashCode(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, WeatherAlertWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Weather Alert")
            .setContentText("Weather conditions update available")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_delete, "Dismiss", dismissPendingIntent)
            .apply {
                if (useDefaultSound) {
                    setDefaults(NotificationCompat.DEFAULT_ALL)
                }
            }
            .build()

        notificationManager?.notify(alertId.hashCode(), notification)
    }
}