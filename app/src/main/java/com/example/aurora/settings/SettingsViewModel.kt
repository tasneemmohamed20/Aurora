package com.example.aurora.settings

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.map.Location
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.toDoubleOrZero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val weatherRepository: WeatherRepository,
    private val context: Context
) : ViewModel() {

    private val _selectedTemperatureUnit = MutableStateFlow(settingsManager.getDisplayTemperatureUnit())
    val selectedTemperatureUnit = _selectedTemperatureUnit.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(settingsManager.language)
    val selectedLanguage = _selectedLanguage.asStateFlow()


    private val _selectedSpeedUnit = MutableStateFlow(settingsManager.getDisplayUnits().second)
    val selectedSpeedUnit = _selectedSpeedUnit.asStateFlow()

    private val _selectedLocationMode = MutableStateFlow(settingsManager.locationMode)
    val selectedLocationMode = _selectedLocationMode.asStateFlow()

    // Add navigation event
    private val _openMap = MutableStateFlow(false)
    val openMap = _openMap.asStateFlow()

    fun updateTemperatureUnit(unit: String) {
        settingsManager.temperatureUnit = unit
        val displayUnits = settingsManager.getDisplayUnits()
        _selectedTemperatureUnit.value = displayUnits.first
        _selectedSpeedUnit.value = displayUnits.second
        refreshWeatherData()
    }

    fun updateLanguage(lang: String) {
        settingsManager.language = lang
        _selectedLanguage.value = settingsManager.language

        // Update configuration immediately
        val locale = Locale(lang)
        val config = context.resources.configuration.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Force activity recreation to apply RTL/LTR changes
        (context as? Activity)?.recreate()

        refreshWeatherData()
    }

    private fun refreshWeatherData() {
        viewModelScope.launch {
            weatherRepository.getAllForecasts().collect { forecasts ->
                forecasts.forEach { forecast ->
                    val location = Location(
                        forecast.city.coord?.lat?.toDoubleOrZero() ?: 0.0,
                        forecast.city.coord?.lon?.toDoubleOrZero() ?: 0.0
                    )
                    weatherRepository.getForecast(location.lat, location.lng)
                }
            }
        }
    }

    fun updateLocationMode(mode: String) {
        settingsManager.locationMode = mode
        _selectedLocationMode.value = settingsManager.locationMode

        // Open map if manual mode is selected
        if (mode == SettingsManager.MODE_MANUAL) {
            _openMap.value = true
        }
    }

    // Reset map navigation
    fun onMapOpened() {
        _openMap.value = false
    }



    class Factory(
        private val settingsManager: SettingsManager,
        private val weatherRepository: WeatherRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(settingsManager, weatherRepository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}