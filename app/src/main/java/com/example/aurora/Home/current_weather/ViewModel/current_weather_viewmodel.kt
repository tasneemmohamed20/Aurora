package com.example.aurora.Home.current_weather.ViewModel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import com.example.aurora.workers.WeatherUpdateWorker
import com.example.aurora.workers.WorkerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.Locale
import java.util.concurrent.TimeUnit

class CurrentWeatherViewModel(
    private val repository: WeatherRepository,
    private val context: Context
) : ViewModel() {
    private val locationHelper = LocationHelper(context)
    private val _weatherState = MutableStateFlow<UiState<CurrentResponse>>(UiState.Loading)
    val weatherState = _weatherState.asStateFlow()
    private val _cityName = MutableStateFlow<String?>(null)
    val cityName = _cityName.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            WorkerUtils.initRepository(repository)
            setupWorkManager()
            setupLocationUpdates()

            // Check cached data
            WorkerUtils.getCachedWeatherData()?.let { cached ->
                _weatherState.value = UiState.Success(cached)
            }
        }
    }

    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weatherWork = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
            30, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeatherUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            weatherWork
        )
    }

    fun setupLocationUpdates() {
        viewModelScope.launch {
            try {
                if (!locationHelper.hasLocationPermission()) {
                    _weatherState.value = UiState.Error("Location permission required")
                    return@launch
                }

                // Initial location update
                locationHelper.getLastKnownLocation()?.let { location ->
                    supervisorScope {
                        try {
                            val weatherDeferred = async(Dispatchers.IO) {
                                fetchWeatherData(location.latitude, location.longitude)
                            }
                            val cityDeferred = async(Dispatchers.IO) {
                                getCityName(location.latitude, location.longitude)
                            }
                            weatherDeferred.await()
                            cityDeferred.await()
                        } catch (e: Exception) {
                            _weatherState.value = UiState.Error("Failed to fetch initial data: ${e.message}")
                        }
                    }
                }

                // Start continuous location updates
                locationHelper.startLocationUpdates()
                locationHelper.getLocationUpdates().collect { location ->
                    location?.let {
                        fetchWeatherData(it.latitude, it.longitude)
                        getCityName(it.latitude, it.longitude)
                        Log.d("WeatherVM", "Fetching weather for location: ${it.latitude}, ${it.longitude}")

                    }
                }
            } catch (e: SecurityException) {
                _weatherState.value = UiState.Error("Location permission denied")
            } catch (e: Exception) {
                _weatherState.value = UiState.Error("Failed to setup location updates: ${e.message}")
            }
        }
    }

    private suspend fun fetchWeatherData(latitude: Double, longitude: Double) {
        try {
            repository.getWeather(latitude, longitude).collect { response ->
                if (response.cod != 200) {
                    _weatherState.value = UiState.Error("API Error: ${response.cod}")
                    return@collect
                }
                _weatherState.value = UiState.Success(response)
                WorkerUtils.cacheWeatherData(response)
            }
        } catch (e: Exception) {
            Log.e("WeatherVM", "Error fetching weather: ${e.message}", e)
            _weatherState.value = UiState.Error("Failed to load weather data: ${e.message}")
        }
    }

    private fun getCityName(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    _cityName.value = addresses.firstOrNull()?.let { address ->
                        address.adminArea
                            ?: address.subAdminArea
                            ?: address.adminArea
                            ?: "Unknown Location"
                    } ?: "Unknown Location"
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                _cityName.value = addresses?.firstOrNull()?.let { address ->
                    address.adminArea
                        ?: address.subAdminArea
                        ?: address.adminArea
                        ?: "Unknown Location"
                } ?: "Unknown Location"
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Error getting city name: ${e.message}")
            _cityName.value = "Unknown Location"
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationHelper.stopLocationUpdates()
    }

    class WeatherViewModelFactory(
        private val repository: WeatherRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CurrentWeatherViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}