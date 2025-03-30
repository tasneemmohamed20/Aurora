package com.example.aurora.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationHelper(internal val context: Context) {
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val _locationFlow = MutableStateFlow<Location?>(null)
    private var locationCallback: LocationCallback? = null

    fun hasLocationPermission(): Boolean = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null
        return try {
            Tasks.await(fusedClient.lastLocation)?.also {
                _locationFlow.value = it
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getLocationUpdates(): Flow<Location?> = _locationFlow.asStateFlow()

    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.d("LocationHelper", "No location permission")
            return
        }

        try {
            // Get last known location immediately
            fusedClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d("LocationHelper", "Last known location: ${it.latitude}, ${it.longitude}")
                    _locationFlow.value = it
                } ?: Log.d("LocationHelper", "No last known location")
            }

            val locationRequest = LocationRequest.Builder(30000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY) // Changed to high accuracy
                .setMinUpdateIntervalMillis(10000) // Reduced interval for testing
                .setMaxUpdateDelayMillis(30000)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        Log.d("LocationHelper", "Location update: ${location.latitude}, ${location.longitude}")
                        _locationFlow.value = location
                    }
                }
            }

            locationCallback?.let { callback ->
                fusedClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    context.mainLooper
                ).addOnSuccessListener {
                    Log.d("LocationHelper", "Location updates requested successfully")
                }.addOnFailureListener { e ->
                    Log.e("LocationHelper", "Failed to request location updates", e)
                }
            }
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Security exception in location updates", e)
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedClient.removeLocationUpdates(callback)
        }
        locationCallback = null
    }

    suspend fun getCurrentLocation(): Location? = suspendCoroutine { continuation ->
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (hasLocationPermission()) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(null)
                    }
            } else {
                continuation.resume(null)
            }
        } catch (_: Exception) {
            continuation.resume(null)
        }
    }
}
