package com.example.aurora.map

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.map.GeocodingResults
import com.example.aurora.data.model.map.Location
import com.example.aurora.data.remote.RetrofitGeoHelper
import com.example.aurora.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapsViewModel(
    private val locationHelper: LocationHelper
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun loadCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            try {
                val lastLocation = locationHelper.getLastKnownLocation()
                val loc = if (lastLocation != null) {
                    Location(lastLocation.latitude, lastLocation.longitude)
                } else {
                    Location(30.0444, 31.2357) // default coordinates
                }
                _location.value = loc
                fetchAddress("${loc.lat},${loc.lng}")
            } catch (e: SecurityException) {
                _uiState.value = MapUiState.Error("Security Exception: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to retrieve current location: ${e.message}")
            }
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
                val key = "AIzaSyDz6_hjwIQjgeaJzmDLKzPGLmkbmJiTayQ"
                val geocodingApi = RetrofitGeoHelper.getRetrofit()
                val response = geocodingApi.getAddressFromGeocoding(latlng, apiKey = key)
                _uiState.value = MapUiState.Success(response.results)
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to fetch address: ${e.message}")
            }
        }
    }

    class Factory(
        private val locationHelper: LocationHelper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
                return MapsViewModel(locationHelper) as T
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