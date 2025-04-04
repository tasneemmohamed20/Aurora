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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import com.example.aurora.utils.toDoubleOrZero
import kotlin.math.abs

class ForecastViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper,
    private val weatherWorkManager: WeatherWorkManager
) : ViewModel() {

    companion object {
        private const val LAST_KNOWN_LOCATION_KEY = "last_known_location"
        private const val HAS_SET_HOME_KEY = "has_set_home_for_location"
        private const val HAS_ASKED_HOME_KEY = "has_asked_for_home"
    }

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val forecastState = _forecastState.asStateFlow()

    private val _cityName = MutableStateFlow<String?>(null)
    val cityName = _cityName.asStateFlow()

    private val _location = MutableStateFlow<android.location.Location?>(null)

    private val _homeDialogVisible = MutableStateFlow(false)
    val homeDialogVisible = _homeDialogVisible.asStateFlow()

    private val sharedPrefs = locationHelper.context.getSharedPreferences("app_prefs", 0)
    private var currentForecastResponse: ForecastResponse? = null
    var shouldUseCurrentLocation = true

    private val _cachedLocation = MutableStateFlow<Location?>(null)
    val cachedLocation = _cachedLocation.asStateFlow()


    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            WorkerUtils.initRepository(repository)
            weatherWorkManager.setupPeriodicWeatherUpdate()

            loadHomeLocation()
        }
    }

    private suspend fun loadHomeLocation() {
        repository.getAllForecasts().firstOrNull()?.let { forecasts ->
            forecasts.find { it.isHome }?.let { homeForecast ->
                val location = Location(
                    homeForecast.city.coord?.lat.toDoubleOrZero(),
                    homeForecast.city.coord?.lon.toDoubleOrZero()
                )
                _cachedLocation.value = location
                fetchForecastData(location.lat, location.lng)
            } ?: setupLocationUpdates() // If no home location found, use current location
        } ?: setupLocationUpdates() // If no forecasts at all, use current location
    }

    fun resetToCurrentLocation() {
        viewModelScope.launch {
            _cachedLocation.value?.let { location ->
                fetchForecastData(location.lat, location.lng)
            } ?: run {
                shouldUseCurrentLocation = true
                locationHelper.getCurrentLocation()?.let { location ->
                    fetchForecastData(location.latitude, location.longitude)
                }
            }
        }
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
                        if (shouldUseCurrentLocation) {
                            val currentLocation = "${it.latitude},${it.longitude}"
                            val lastLocation = sharedPrefs.getString(LAST_KNOWN_LOCATION_KEY, null)
                            val hasSetHomeForLocation =
                                sharedPrefs.getBoolean(HAS_SET_HOME_KEY, false)

                            // Only show dialog if never asked before or location changed and home not set
                            if (!sharedPrefs.getBoolean(HAS_ASKED_HOME_KEY, false) ||
                                (lastLocation != null && lastLocation != currentLocation && !hasSetHomeForLocation)
                            ) {
                                _homeDialogVisible.value = true
                                sharedPrefs.edit {
                                    putBoolean(HAS_ASKED_HOME_KEY, true)
                                        .putString(LAST_KNOWN_LOCATION_KEY, currentLocation)
                                }
                            }
                                fetchForecastData(it.latitude, it.longitude)
                        }
                    }
                }
            } catch (e: Exception) {
                _forecastState.value = ForecastUiState.Error("Failed to setup location updates: ${e.message}")
            }
        }
    }

    fun confirmHomeLocation() {
        viewModelScope.launch {
            currentForecastResponse?.let { forecast ->
                val updatedForecast = forecast.copy(isHome = true)
                repository.insertForecast(updatedForecast)
                // Update current response to reflect the change
                currentForecastResponse = updatedForecast
                // Update UI state without fetching new data
                _forecastState.value = ForecastUiState.Success(processHourlyData(updatedForecast))
            }
            _homeDialogVisible.value = false
            sharedPrefs.edit {
                putBoolean(HAS_SET_HOME_KEY, true)
            }
        }
    }


    fun dismissHomeDialog() {
        _homeDialogVisible.value = false
    }


    fun updateLocation(location: Location) {
        viewModelScope.launch {
            shouldUseCurrentLocation = false
            _cachedLocation.value = location

            try {
                if (locationHelper.context.hasNetworkConnection()) {
                    fetchForecastData(location.lat, location.lng)
                } else {
                    getForecastFromDatabase(location.lat, location.lng)
                }

                val currentLocation = "${location.lat},${location.lng}"
                val lastLocation = sharedPrefs.getString(LAST_KNOWN_LOCATION_KEY, null)
                val hasSetHomeForLocation = sharedPrefs.getBoolean(HAS_SET_HOME_KEY, false)

                if (lastLocation != currentLocation && !hasSetHomeForLocation) {
                    _homeDialogVisible.value = true
                    sharedPrefs.edit {
                        putString(LAST_KNOWN_LOCATION_KEY, currentLocation)
                    }
                }
            } catch (e: Exception) {
                _forecastState.value = ForecastUiState.Error("Failed to update location: ${e.message}")
            }
        }
    }

    private suspend fun fetchForecastData(latitude: Double, longitude: Double) {
//        _cityName.value = null

        try {
            if (locationHelper.context.hasNetworkConnection()) {
                repository.getForecast(latitude, longitude).collect { response ->
                    // Don't set home flag here - wait for user confirmation
                    currentForecastResponse = response.copy(isHome = false)
                    if (response.cod == "200") {
                        repository.insertForecast(response.copy(isHome = false))
                        val processedData = processHourlyData(response)
                        _forecastState.value = ForecastUiState.Success(processedData)
                        _cityName.value = response.city.name
                    } else {
                        getForecastFromDatabase(latitude, longitude)
                    }
                }
            } else {
                getForecastFromDatabase(latitude, longitude)
            }
        } catch (e: Exception) {
            getForecastFromDatabase(latitude, longitude)
        }
    }

    private suspend fun getForecastFromDatabase(latitude: Double, longitude: Double) {
        repository.getAllForecasts().collect { forecasts ->
            Log.d("ForecastViewModel", "Collected forecasts: $forecasts")
            val forecast = forecasts.find { forecast ->
                // Use a small epsilon for floating point comparison
                val epsilon = 0.0001
                val lat = forecast.city.coord?.lat ?: 0.0
                val lon = forecast.city.coord?.lon ?: 0.0

                abs(lat.toDoubleOrZero() - latitude.toDoubleOrZero()) < epsilon &&
                        abs(lon.toDoubleOrZero() - longitude.toDoubleOrZero()) < epsilon
            }

            if (forecast != null) {
                Log.d("ForecastViewModel", "Found forecast: $forecast")
                currentForecastResponse = forecast
                val processedData = processHourlyData(forecast)
                _forecastState.value = ForecastUiState.Success(processedData)
                _cityName.value = forecast.city.name
            } else {
                // First try to get the home location if available
                val homeForecast = forecasts.find { it.isHome }
                if (homeForecast != null) {
                    currentForecastResponse = homeForecast
                    val processedData = processHourlyData(homeForecast)
                    _forecastState.value = ForecastUiState.Success(processedData)
                    _cityName.value = homeForecast.city.name
                } else if (forecasts.isNotEmpty()) {
                    // If no home location, use the most recent forecast
                    val mostRecent = forecasts.first()
                    currentForecastResponse = mostRecent
                    val processedData = processHourlyData(mostRecent)
                    _forecastState.value = ForecastUiState.Success(processedData)
                    _cityName.value = mostRecent.city.name
                } else {
                    _forecastState.value = ForecastUiState.Error("No cached data available")
                }
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