package com.example.aurora.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.model.forecast.ListItem
import com.example.aurora.data.model.map.Location
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import com.example.aurora.utils.hasNetworkConnection
import com.example.aurora.workers.WeatherWorkManager
import com.example.aurora.workers.WorkerUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ForecastViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper,
    private val weatherWorkManager: WeatherWorkManager
) : ViewModel() {

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val forecastState = _forecastState.asStateFlow()

    private val _cityName = MutableStateFlow<String?>(null)
    val cityName = _cityName.asStateFlow()

    private val _location = MutableStateFlow<android.location.Location?>(null)

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            WorkerUtils.initRepository(repository)
            weatherWorkManager.setupPeriodicWeatherUpdate()
            setupLocationUpdates()
        }
    }


    fun setupLocationUpdates() {
        viewModelScope.launch {
            try {
                if (!locationHelper.hasLocationPermission()) {
                    _forecastState.value = ForecastUiState.Error("Location permission required")
                    return@launch
                }

                // Start location updates
                locationHelper.startLocationUpdates()

                // Collect location updates
                locationHelper.getLocationUpdates().collect { location ->
                    location?.let {
                        fetchForecastData(it.latitude, it.longitude)
                    }
                }
            } catch (e: Exception) {
                _forecastState.value = ForecastUiState.Error("Failed to setup location updates: ${e.message}")
            }
        }
    }

    fun updateLocation(location: Location) {
        viewModelScope.launch {
            try {
                fetchForecastData(location.lat, location.lng)
            } catch (e: Exception) {
                _forecastState.value = ForecastUiState.Error("Failed to update location: ${e.message}")
            }
        }
    }

    private suspend fun fetchForecastData(latitude: Double, longitude: Double) {
        try {
            val isConnected = locationHelper.context.hasNetworkConnection()

            if (isConnected) {
                try {
                    // Try API call first
                    repository.getForecast(latitude, longitude).collect { response ->
                        if (response.cod == "200") {
                            // Cache the response in Room
                            repository.insertForecast(response)
                            val processedData = processHourlyData(response)
                            _forecastState.value = ForecastUiState.Success(processedData)
                            _cityName.value = response.city.name
                        } else {
                            getForecastFromDatabase()
                        }
                    }
                } catch (_: Exception) {
                    // If API call fails, fall back to database
                    getForecastFromDatabase()
                }
            } else {
                getForecastFromDatabase()
            }
        } catch (e: Exception) {
            _forecastState.value = ForecastUiState.Error("Failed to load forecast data: ${e.message}")
        }
    }

    private suspend fun getForecastFromDatabase() {
        Log.i("ForecastViewModel", "Fetching from Database")
        repository.getAllForecasts().collect { forecasts ->
            if (forecasts.isNotEmpty()) {
                val latestForecast = forecasts.last()
                val processedData = processHourlyData(latestForecast)
                _forecastState.value = ForecastUiState.Success(processedData)
                _cityName.value = latestForecast.city.name
            } else {
                _forecastState.value = ForecastUiState.Error("No cached data available")
            }
        }
    }

    private fun processHourlyData(response: ForecastResponse): MutableList<ListItem> {
        val hourlyDataList = mutableListOf<ListItem>()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

         response.list?.forEach { item ->
            item?.let {
                val timestamp = it.dt?.toLong()?.times(1000) ?: return@forEach

                val time = when {
                    hourlyDataList.isEmpty() -> "Now"
                    else -> timeFormat.format(Date(timestamp))
                }

                val dateTxt = item.dtTxt?.split(" ")?.firstOrNull() ?:
                dateFormat.format(Date(timestamp))

                hourlyDataList.add(
                    item.copy(
                        dtTxt = "$time, $dateTxt"
                    )
                )
            }
        }
        return hourlyDataList
    }

    fun onConfigurationChanged() {
        // Store current location before setting loading state
        val currentLocation = _location.value

        _forecastState.value = ForecastUiState.Loading

        // Only fetch if we have a location
        currentLocation?.let { loc ->
            viewModelScope.launch {
                try {
                    fetchForecastData(loc.latitude, loc.longitude)
                } catch (e: Exception) {
                    _forecastState.value = ForecastUiState.Error("Failed to refresh data: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationHelper.stopLocationUpdates()
    }

    class Factory(
        private val repository: WeatherRepository,
        private val locationHelper: LocationHelper,
        private val weatherWorkManager: WeatherWorkManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
                return ForecastViewModel(repository, locationHelper, weatherWorkManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ForecastUiState {
    data object Loading : ForecastUiState()
    data class Success(val data: List<ListItem>) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}
