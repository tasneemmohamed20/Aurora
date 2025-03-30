package com.example.aurora.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.map.GeocodingResults
import com.example.aurora.data.model.map.Location
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MapsViewModel(
    private val locationHelper: LocationHelper,
    private val weatherRepository: WeatherRepository,
    private val placesClient: PlacesClient

) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Initial)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val _predictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val predictions: StateFlow<List<AutocompletePrediction>> = _predictions.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private var tempLocation: Location? = null


    init {
        if (locationHelper.hasLocationPermission()) {
            viewModelScope.launch {
                // Get location once
                locationHelper.getCurrentLocation()?.let { loc ->
                    val newLoc = Location(loc.latitude, loc.longitude)
                    _location.value = newLoc
                    fetchAddress("${newLoc.lat},${newLoc.lng}")
                } ?: run {
                    _uiState.value = MapUiState.Error("Could not get current location")
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

    fun searchPlaces(query: String) {
        viewModelScope.launch {
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .build()

                val predictions = suspendCoroutine { continuation ->
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            continuation.resume(response.autocompletePredictions)
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                }
                _predictions.value = predictions
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to search places: ${e.message}")
            }
        }
    }

    fun getPlaceDetails(placeId: String) {
        viewModelScope.launch {
            try {
                val placeFields = listOf(Place.Field.LAT_LNG)
                val request = FetchPlaceRequest.newInstance(placeId, placeFields)

                val place = suspendCoroutine { continuation ->
                    placesClient.fetchPlace(request)
                        .addOnSuccessListener { response ->
                            continuation.resume(response.place)
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                }

                place.latLng?.let { latLng ->
                    tempLocation = Location(latLng.latitude, latLng.longitude)
                    updateLocation(tempLocation!!)
                    _showDialog.value = true
                }
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to get place details: ${e.message}")
            }
        }
    }

    fun addToFavorites() {
        viewModelScope.launch {
            try {
                tempLocation?.let { location ->
                    weatherRepository.getForecast(location.lat, location.lng)
                        .collect { forecast ->
                            weatherRepository.insertForecast(forecast)
                        }
                }
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to save location: ${e.message}")
            } finally {
                _showDialog.value = false
                tempLocation = null
            }
        }
    }

    fun dismissDialog() {
        _showDialog.value = false
        tempLocation = null
    }


    class Factory(
        private val locationHelper: LocationHelper,
        private val weatherRepository: WeatherRepository,
        private val placesClient: PlacesClient
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
                return MapsViewModel(locationHelper, weatherRepository, placesClient) as T
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