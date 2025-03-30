package com.example.aurora.home

import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.apply

class ForecastViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper,
    private val weatherWorkManager: WeatherWorkManager
) : ViewModel() {

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val forecastState = _forecastState.asStateFlow()

    private val _cityName = MutableStateFlow<String?>(null)
    val cityName = _cityName.asStateFlow()

    // Hold the latest forecast response for marking as home
    private var currentForecastResponse: ForecastResponse? = null

    private val _location = MutableStateFlow<android.location.Location?>(null)

    private val sharedPrefs = locationHelper.context.getSharedPreferences("app_prefs", 0)
    private val hasAskedForHomeKey = "has_asked_for_home"
    private val lastKnownLocationKey = "last_known_location"
    private val hasSetHomeForLocationKey = "has_set_home_for_location"

    var shouldUseCurrentLocation = true
    private val _selectedLocation = MutableStateFlow<Location?>(null)


    // Update homeDialogVisible to private
    private val _homeDialogVisible = MutableStateFlow(false)
    val homeDialogVisible = _homeDialogVisible.asStateFlow()

    private var isReturningFromFavorites = false
    private var isFromFavorites = false
    private var isFetchingData = false


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

    fun resetToCurrentLocation() {
        shouldUseCurrentLocation = true
    }

    fun setupLocationUpdates() {
        viewModelScope.launch {
            try {
                if (!locationHelper.hasLocationPermission()) {
                    _forecastState.value = ForecastUiState.Error("Location permission required")
                    return@launch
                }

                locationHelper.startLocationUpdates()
                locationHelper.getLocationUpdates().collect { location ->
                    location?.let {
                        if (shouldUseCurrentLocation && !isFromFavorites) {
                            isReturningFromFavorites = false
                            val currentLocation = "${it.latitude},${it.longitude}"
                            val lastLocation = sharedPrefs.getString(lastKnownLocationKey, null)
                            val hasSetHomeForLocation =
                                sharedPrefs.getBoolean(hasSetHomeForLocationKey, false)

                            // Only show dialog if never asked before or location changed and home not set
                            if (!sharedPrefs.getBoolean(hasAskedForHomeKey, false) ||
                                (lastLocation != null && lastLocation != currentLocation && !hasSetHomeForLocation)
                            ) {
                                _homeDialogVisible.value = true
                                sharedPrefs.edit()
                                    .putBoolean(hasAskedForHomeKey, true)
                                    .putString(lastKnownLocationKey, currentLocation)
                                    .apply()
                            }
                            isFromFavorites = false
                            try {
                                fetchForecastData(it.latitude, it.longitude)
                            } finally {
                                isFetchingData = false
                            }                        }
                    }
                }
            } catch (e: Exception) {
                _forecastState.value = ForecastUiState.Error("Failed to setup location updates: ${e.message}")
            }
        }
    }

    fun confirmHomeLocation() {
        viewModelScope.launch {
            try {
                currentForecastResponse?.let { response ->
                    repository.getAllForecasts()
                        .firstOrNull()?.let { forecasts ->
                            forecasts.forEach { forecast ->
                                if (forecast.isHome) {
                                    repository.deleteForecast(forecast)
                                }
                            }
                            repository.insertForecast(response.copy(isHome = true))
                            // Mark that home has been set for this location
                            sharedPrefs.edit()
                                .putBoolean(hasSetHomeForLocationKey, true)
                                .apply()
                        }
                }
            } catch (e: Exception) {
                // Handle error
            }
            _homeDialogVisible.value = false
        }
    }

    fun dismissHomeDialog() {
        _homeDialogVisible.value = false
    }

    fun onReturnFromFavorites() {
        isReturningFromFavorites = true
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading
            _cityName.value = null
            shouldUseCurrentLocation = true
            // Force location update
            locationHelper.startLocationUpdates()
            setupLocationUpdates()
        }
    }

    fun updateLocation(location: Location) {
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading
            shouldUseCurrentLocation = false // Set this first
            isFromFavorites = true // Then set this
            try {
                fetchForecastData(location.lat, location.lng)
                val currentLocation = "${location.lat},${location.lng}"
                val lastLocation = sharedPrefs.getString(lastKnownLocationKey, null)
                val hasSetHomeForLocation = sharedPrefs.getBoolean(hasSetHomeForLocationKey, false)

                // Only show dialog if location changed and home not set
                if (lastLocation != currentLocation && !hasSetHomeForLocation) {
                    _homeDialogVisible.value = true
                    sharedPrefs.edit()
                        .putString(lastKnownLocationKey, currentLocation)
                        .apply()
                }

            } catch (e: Exception) {
                _forecastState.value = ForecastUiState.Error("Failed to update location: ${e.message}")
            }
        }
    }

    private suspend fun fetchForecastData(latitude: Double, longitude: Double) {
        Log.d("ForecastViewModel", "Fetching forecast data for: $latitude, $longitude")
        _forecastState.value = ForecastUiState.Loading
        _cityName.value = null

        try {
            val isConnected = locationHelper.context.hasNetworkConnection()

            if (isConnected) {
                try {
                    repository.getForecast(latitude, longitude).collect { response ->
                        // Store current forecast response for potential home marking
                        currentForecastResponse = response
                        if (response.cod == "200") {
                            repository.insertForecast(response)
                            val processedData = processHourlyData(response)
                            _forecastState.value = ForecastUiState.Success(processedData)
                            _cityName.value = response.city.name
                        } else {
                            getForecastFromDatabase()
                        }
                    }
                } catch (_: Exception) {
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
                currentForecastResponse = latestForecast
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
        val currentLocation = _location.value
        _forecastState.value = ForecastUiState.Loading
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

    public override fun onCleared() {
        super.onCleared()
        locationHelper.stopLocationUpdates()
    }

    fun resetLocationFlags() {
        isFromFavorites = false
        isReturningFromFavorites = false
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