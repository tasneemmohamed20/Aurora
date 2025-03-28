package com.example.aurora.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.map.GeocodingResults
import com.example.aurora.data.model.map.Location
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapsViewModel(
    private val locationHelper: LocationHelper,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Initial)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    init {
        if (locationHelper.hasLocationPermission()) {
            // Start continuous location updates
            locationHelper.startLocationUpdates()
            viewModelScope.launch {
                locationHelper.getLocationUpdates().collect { loc ->
                    loc?.let {
                        val newLoc = Location(it.latitude, it.longitude)
                        _location.value = newLoc
                        fetchAddress("${newLoc.lat},${newLoc.lng}")
                    }
                }
            }
        } else {
            _uiState.value = MapUiState.Error("Location permission not granted")
        }
    }

    fun updateLocation(location: Location) {
        _location.value = location
        fetchAddress("${location.lat},${location.lng}")
    }

    private fun fetchAddress(latlng: String) {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            try {
                weatherRepository.getAddressFromGeocoding(latlng, "")
                    .collect { address ->
                        // Wrap the address string in GeocodingResults to satisfy uiState
                        _uiState.value = MapUiState.Success(listOf(GeocodingResults(address)))
                    }
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to fetch address: ${e.message}")
            }
        }
    }

    class Factory(
        private val locationHelper: LocationHelper,
        private val weatherRepository: WeatherRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
                return MapsViewModel(locationHelper, weatherRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class MapUiState {
    object Initial : MapUiState()
    object Loading : MapUiState()
    data class Success(val addresses: List<GeocodingResults>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}