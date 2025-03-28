// File: app/src/main/java/com/example/aurora/map/MapScreen.kt
package com.example.aurora.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aurora.data.model.map.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    location: Location,
    onLocationSelected: (Location) -> Unit
) {
    val viewModel: MapsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = MapsViewModel.Factory(
            androidx.compose.ui.platform.LocalContext.current.let { ctx ->
                com.example.aurora.utils.LocationHelper(ctx)
            }
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val currentLocation by viewModel.location.collectAsState()

    // Initialize marker state with provided location to avoid a crash.
    val markerState = remember {
        MarkerState(LatLng(location.lat, location.lng))
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerState.position, 10f)
    }

    // When currentLocation updates, update marker and camera positions.
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            val newLatLng = LatLng(loc.lat, loc.lng)
            markerState.position = newLatLng
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 10f))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    markerState.position = latLng
                    viewModel.updateLocation(Location(latLng.latitude, latLng.longitude))
                }
            ) {
                Marker(
                    state = markerState,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }
            when (uiState) {
                is MapUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                is MapUiState.Success -> {
                    val address = (uiState as MapUiState.Success).addresses.firstOrNull()
                    address?.let {
                        Button(
                            onClick = {
                                onLocationSelected(
                                    Location(markerState.position.latitude, markerState.position.longitude)
                                )
                            },
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(text = "Select ${it.formattedAddress}")
                        }
                    }
                }
                is MapUiState.Error -> {
                    Text(
                        text = (uiState as MapUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> Unit
            }
        }
    }
}