package com.example.aurora.home.current_weather.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import com.example.aurora.workers.WeatherWorkManager
import com.example.aurora.workers.WorkerUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CurrentWeatherViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper,
    private val weatherWorkManager: WeatherWorkManager

) : ViewModel() {
    private val _weatherState = MutableStateFlow<UiState<CurrentResponse>>(UiState.Loading)
    val weatherState = _weatherState.asStateFlow()


    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            WorkerUtils.initRepository(repository)
//            setupWorkManager()
            weatherWorkManager.setupPeriodicWeatherUpdate()
            setupLocationUpdates()
        }
    }

//    private fun setupWorkManager() {
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//
//        val weatherWork = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
//            30, TimeUnit.MINUTES,
//            5, TimeUnit.MINUTES
//        ).setConstraints(constraints)
//            .build()
//
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//            WeatherUpdateWorker::class.java.simpleName,
//            ExistingPeriodicWorkPolicy.UPDATE,
//            weatherWork
//        )
//    }

    fun setupLocationUpdates() {
        viewModelScope.launch {
            try {
                if (!locationHelper.hasLocationPermission()) {
                    Log.d("WeatherVM", "No location permission")
                    _weatherState.value = UiState.Error("Location permission required")
                    return@launch
                }

                Log.d("WeatherVM", "Starting location updates")
                locationHelper.startLocationUpdates()

                // Collect location updates
                locationHelper.getLocationUpdates().collect { location ->
                    Log.d("WeatherVM", "Location update received: $location")
                    location?.let {
                        Log.d("WeatherVM", "Fetching weather for ${it.latitude}, ${it.longitude}")
                        fetchWeatherData(it.latitude, it.longitude)
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherVM", "Failed to setup location updates", e)
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

    override fun onCleared() {
        super.onCleared()
        locationHelper.stopLocationUpdates()
    }

    class WeatherViewModelFactory(
        private val repository: WeatherRepository,
        private val locationHelper: LocationHelper,
        private val weatherWorkManager: WeatherWorkManager  // ✅ Better approach
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CurrentWeatherViewModel(repository, locationHelper, weatherWorkManager) as T
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