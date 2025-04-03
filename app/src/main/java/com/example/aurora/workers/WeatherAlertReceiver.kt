package com.example.aurora.workers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class WeatherAlertReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            WeatherAlertWorker.ACTION_DISMISS_ALERT -> {
                val alertId = intent.getStringExtra(WeatherAlertWorker.EXTRA_ALERT_ID)
                if (alertId != null) {
                    val notificationManager = context.getSystemService(NotificationManager::class.java)
                    notificationManager.cancel(alertId.hashCode())
                    WorkManager.getInstance(context).cancelUniqueWork("weatherAlert_$alertId")
                }
            }
            WeatherAlertWorker.ACTION_SHOW_ALERT -> {
                val alertId = intent.getStringExtra(WeatherAlertWorker.EXTRA_ALERT_ID)
                val useDefaultSound = intent.getBooleanExtra("useDefaultSound", true)
                if (alertId != null) {

                    val inputData = Data.Builder()
                        .putString(WeatherAlertWorker.EXTRA_ALERT_ID, alertId)
                        .putBoolean("useDefaultSound", useDefaultSound)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
                        .setInputData(inputData)
                        .build()

                    WorkManager.getInstance(context)
                        .enqueue(workRequest)
                }
            }
        }

    }
}
