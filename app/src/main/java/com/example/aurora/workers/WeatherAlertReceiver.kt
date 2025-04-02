package com.example.aurora.workers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.example.aurora.data.local.AppDatabase
import com.example.aurora.data.local.LocalDataSourceImp
import com.example.aurora.data.remote.RemoteDataSourceImp
import com.example.aurora.data.repo.WeatherRepositoryImp
import com.example.aurora.settings.SettingsManager

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
                    val repository = WeatherRepositoryImp.getInstance(
                        RemoteDataSourceImp(),
                        LocalDataSourceImp(AppDatabase.getInstance(context).getForecastDao()),
                        context,
                        SettingsManager(context)
                    )
                    WeatherAlertWorker(context, repository).showWeatherAlert(alertId, useDefaultSound)
                }
            }
        }

    }
}
