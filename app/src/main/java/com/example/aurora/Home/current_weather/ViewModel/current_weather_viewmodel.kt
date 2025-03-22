package com.example.aurora.Home.current_weather.ViewModel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.repo.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class CurrentWeatherViewModel(
    private val repository: WeatherRepository,
    private val context: Context
) : ViewModel() {
    private val _weatherState = MutableStateFlow<UiState<CurrentResponse>>(UiState.Loading)
    val weatherState = _weatherState.asStateFlow()
    private val _cityName = MutableStateFlow<String?>(null)
    val cityName = _cityName.asStateFlow()

    init {
        getWeather()
    }

    private fun getWeather() {
        viewModelScope.launch {
            try {
                repository.getWeather().collect { response ->
                    _weatherState.value = UiState.Success(response)
                    Log.i("Debug", "Coordinates = ${response.coord?.lat}, ${response.coord?.lon}")
                    val lat = (response.coord?.lat as? Double) ?: return@collect
                    val lon = (response.coord?.lon as? Double) ?: return@collect
                    Log.i("Debug", "Coordinates = $lat, $lon")
                    // Use coordinates from the API response
                    response.coord?.let { coord ->
                        (coord.lat as? Double)?.let { lat ->
                            (coord.lon as? Double)?.let { lon ->
                                getCityName(lat, lon)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _weatherState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun getCityName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        _cityName.value = addresses.firstOrNull()?.let { address ->
                            address.locality // City name
                                ?: address.subAdminArea // District
                                ?: address.adminArea // State
                                ?: "Unknown Location"
                        } ?: "Unknown Location"
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    _cityName.value = addresses?.firstOrNull()?.let { address ->
                        address.locality // City name
                            ?: address.subAdminArea // District
                            ?: address.adminArea // State
                            ?: "Unknown Location"
                    } ?: "Unknown Location"
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Error getting city name: ${e.message}")
                _cityName.value = "Unknown Location"
            }
        }
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