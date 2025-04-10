package com.example.aurora.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.R
import com.example.aurora.data.model.map.GeocodingResults
import com.example.aurora.data.model.map.Location
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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


    private val _source = MutableStateFlow("favorites")
    val source = _source.asStateFlow()

    fun setSource(newSource: String) {
        _source.value = newSource
    }


    init {
        viewModelScope.launch {
            // Increase delay to ensure permission status is updated
            delay(2000)
            getCurrentLocation()
        }
    }
    fun getDialogMessage(context: Context): String {
        return if (_source.value == "settings") {
            context.getString(R.string.setHomeLocation_msg)
        } else {
            context.getString(R.string.addToFavMsg)
        }
    }

    fun openDialog() {
        _showDialog.value = true
    }

    fun handleDialogConfirm(onComplete: () -> Unit) {
        if (_source.value == "settings") {
            addToHomeLocation(onComplete)
        } else {
            addToFavorites(onComplete)
        }
    }

    private fun addToHomeLocation(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                tempLocation?.let { location ->
                    // First, remove existing home location if any
                    weatherRepository.getAllForecasts().firstOrNull()?.let { forecasts ->
                        forecasts.find { it.isHome }?.let { oldHome ->
                            weatherRepository.deleteForecast(oldHome.city.name)
                            weatherRepository.insertForecast(oldHome.copy(isHome = false))
                        }
                    }

                    // Then set the new home location
                    weatherRepository.getForecast(location.lat, location.lng)
                        .collect { forecast ->
                            // Explicitly set isHome to true
                            val updatedForecast = forecast.copy(isHome = true)
                            weatherRepository.insertForecast(updatedForecast)
                            // Break the flow after first emission
                            return@collect
                        }
                }
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to set home location: ${e.message}")
            } finally {
                _showDialog.value = false
                tempLocation = null
                onComplete() // Call completion handler
            }
        }
    }

    private fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                if (!locationHelper.hasLocationPermission()) {
                    _uiState.value = MapUiState.Error("Location permission not granted")
                    return@launch
                }

                locationHelper.getCurrentLocation()?.let { loc ->
                    val newLoc = Location(loc.latitude, loc.longitude)
                    _location.value = newLoc
                    fetchAddress("${newLoc.lat},${newLoc.lng}")
                } ?: run {
                    // Try getting last location as fallback
                    locationHelper.getLastLocation()?.let { lastLoc ->
                        val newLoc = Location(lastLoc.latitude, lastLoc.longitude)
                        _location.value = newLoc
                        fetchAddress("${newLoc.lat},${newLoc.lng}")
                    } ?: run {
                        _uiState.value = MapUiState.Error("Could not get current location")
                    }
                }
            } catch (_: SecurityException) {
                _uiState.value = MapUiState.Error("Location permission not granted")
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to get location: ${e.message}")
            }
        }
    }

    // Add method to retry getting location
    fun retryLocationUpdate() {
        getCurrentLocation()
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

    internal fun addToFavorites(onComplete: () -> Unit) {
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
                onComplete() // Call completion handler
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