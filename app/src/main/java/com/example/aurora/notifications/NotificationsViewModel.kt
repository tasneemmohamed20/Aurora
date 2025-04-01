package com.example.aurora.notifications

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.workers.WeatherWorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class NotificationsViewModel(
    private val repository: WeatherRepository,
    private val workManager: WeatherWorkManager,
    private val context: Context
) : ViewModel() {
    private val _scheduledAlerts = MutableStateFlow<List<WeatherAlertSettings>>(emptyList())
    val scheduledAlerts: StateFlow<List<WeatherAlertSettings>> = _scheduledAlerts.asStateFlow()

    init {
        loadAlerts()
    }


    fun updateAlerts(){
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            try {
                repository.getAllAlerts().collect { alerts ->
                    val currentTime = System.currentTimeMillis()

                    // Split alerts into valid and expired
                    val (expired, valid) = alerts.partition { alert ->
                        alert.startTime + alert.duration <= currentTime
                    }

                    // Delete expired alerts first
                    expired.forEach { alert ->
                        Log.d("NotificationsVM", "Deleting expired alert: ${alert.id}")
                        val result = repository.deleteAlert(alert)
                        if (result > 0) {
                            workManager.cancelWeatherAlert(alert.id)
                            Log.d("NotificationsVM", "Successfully deleted expired alert: ${alert.id}")
                        } else {
                            Log.e("NotificationsVM", "Failed to delete expired alert: ${alert.id}")
                        }
                    }

                    // Update UI with valid alerts
                    _scheduledAlerts.value = valid
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error loading alerts", e)
            }
        }
    }

    fun scheduleAlert(settings: WeatherAlertSettings) {
        viewModelScope.launch {
            try {
                if (!Settings.canDrawOverlays(context)) {
                    requestOverlayPermission()
                    return@launch
                }

                // Add insertion result check
                val result = repository.insertAlert(settings)
                if (result > 0) {
                    workManager.scheduleWeatherAlert(
                        settings.id,
                        settings.duration,
                        settings.useDefaultSound
                    )
                    // Refresh alerts after successful insertion
                    loadAlerts()
                } else {
                    Log.e("NotificationsVM", "Failed to insert alert")
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error scheduling alert", e)
                // Consider updating UI state to show error
            }
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun cancelAlert(id: String) {
        viewModelScope.launch {
            try {
                scheduledAlerts.value.find { it.id == id }?.let { alert ->
                    val result = repository.deleteAlert(alert)
                    if (result > 0) {
                        workManager.cancelWeatherAlert(alert.id)
                        // Refresh alerts after successful deletion
                        loadAlerts()
                    } else {
                        Log.e("NotificationsVM", "Failed to delete alert")
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error canceling alert", e)
            }
        }
    }

    private suspend fun deleteAlert(alert: WeatherAlertSettings) {
        repository.deleteAlert(alert)
        workManager.cancelWeatherAlert(alert.id)
    }

    class Factory(
        private val repository: WeatherRepository,
        private val workManager: WeatherWorkManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
                return NotificationsViewModel(repository, workManager, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}