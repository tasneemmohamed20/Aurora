package com.example.aurora.home.hourly_daily_forecast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.hourly_daily.HourlyDailyResponse
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HourlyForecastViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val forecastState = _forecastState.asStateFlow()

//    init {
//        setupLocationUpdates()
//    }

    fun setupLocationUpdates() {
        viewModelScope.launch {
            try {
                if (!locationHelper.hasLocationPermission()) {
                    _forecastState.value = ForecastUiState.Error("Location permission required")
                    return@launch
                }

                try {
                    // Get initial location with explicit permission check
                    if (locationHelper.hasLocationPermission()) {
                        locationHelper.getLastKnownLocation()?.let { location ->
                            fetchForecastData(location.latitude, location.longitude)
                        }
                    }

                    // Start location updates
                    locationHelper.startLocationUpdates()

                    // Collect location updates
                    locationHelper.getLocationUpdates().collect { location ->
                        location?.let {
                            fetchForecastData(it.latitude, it.longitude)
                        }
                    }
                } catch (e: SecurityException) {
                    _forecastState.value = ForecastUiState.Error("Location permission denied")
                } catch (e: Exception) {
                    _forecastState.value = ForecastUiState.Error("Failed to setup location updates: ${e.message}")
                }
            } catch (e: Exception) {
                _forecastState.value = ForecastUiState.Error("Failed to setup location updates: ${e.message}")
            }
        }
    }

    private suspend fun fetchForecastData(latitude: Double, longitude: Double) {
        try {
            repository.getForecast(latitude, longitude).collect { response ->
                if (response.cod == "200") {
                    _forecastState.value = ForecastUiState.Success(
                        processHourlyData(response)
                    )
                } else {
                    _forecastState.value = ForecastUiState.Error("API Error: ${response.cod}")
                }
            }
        } catch (e: Exception) {
            _forecastState.value = ForecastUiState.Error("Failed to load forecast data: ${e.message}")
        }
    }

    private fun processHourlyData(response: HourlyDailyResponse): List<HourlyForecastData> {
        val hourlyDataList = mutableListOf<HourlyForecastData>()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        response.list?.forEach { item ->
            item?.let {
                val timestamp = it.dt?.toLong()?.times(1000) ?: return@forEach

                val time = when {
                    hourlyDataList.isEmpty() -> "Now"
                    else -> timeFormat.format(Date(timestamp))
                }

                // Use the dtTxt from API directly instead of formatting timestamp
                val dt_txt = item.dtTxt?.split(" ")?.firstOrNull() ?:
                dateFormat.format(Date(timestamp))

                hourlyDataList.add(
                    HourlyForecastData(
                        time = time,
                        dt_txt = dt_txt,
                        temperature = (it.main?.temp as? Double)?.toInt() ?: 0,
                        weatherIcon = it.weather?.firstOrNull()?.icon ?: "",
                        weatherDescription = it.weather?.firstOrNull()?.description ?: ""
                    )
                )
            }
        }
        return hourlyDataList
    }

    override fun onCleared() {
        super.onCleared()
        locationHelper.stopLocationUpdates()
    }

    class Factory(
        private val repository: WeatherRepository,
        private val locationHelper: LocationHelper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HourlyForecastViewModel::class.java)) {
                return HourlyForecastViewModel(repository, locationHelper) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ForecastUiState {
    data object Loading : ForecastUiState()
    data class Success(val data: List<HourlyForecastData>) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}

data class HourlyForecastData(
    val time: String,
    val dt_txt: String,
    val temperature: Int,
    val weatherIcon: String,
    val weatherDescription: String
)