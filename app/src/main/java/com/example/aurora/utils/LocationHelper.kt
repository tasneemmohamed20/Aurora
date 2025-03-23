package com.example.aurora.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
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

class LocationHelper(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val _locationFlow = MutableStateFlow<Location?>(null)
    private var locationCallback: LocationCallback? = null

    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

        return try {
            Tasks.await(fusedClient.lastLocation)?.also {
                _locationFlow.value = it
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getLocationUpdates(): Flow<Location?> = _locationFlow.asStateFlow()

    fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        try {
            val locationRequest = LocationRequest.Builder(30000)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMinUpdateIntervalMillis(30000)
                .setMaxUpdateDelayMillis(120000)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        _locationFlow.value = location
                    }
                }
            }

            locationCallback?.let { callback ->
                fusedClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    context.mainLooper
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedClient.removeLocationUpdates(callback)
        }
        locationCallback = null
    }
}