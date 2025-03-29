package com.example.aurora.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.aurora.data.model.map.Location
import com.example.aurora.ui.components.CustomSearchBar
import com.example.aurora.ui.components.SearchBarState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    location: Location,
    onLocationSelected: (Location) -> Unit,
    viewModel : MapsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLocation by viewModel.location.collectAsState()
    val searchQuery = remember { mutableStateOf("") }

    val markerState = remember {
        MarkerState(LatLng(location.lat, location.lng))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerState.position, 10f)
    }

    val predictions by viewModel.predictions.collectAsState()


    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            val newLatLng = LatLng(loc.lat, loc.lng)
            markerState.position = newLatLng
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 10f))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CustomSearchBar(
            state = SearchBarState(
                query = searchQuery.value,
                onQueryChange = { newQuery ->
                    searchQuery.value = newQuery
                    viewModel.searchPlaces(newQuery)
                },
                onActiveChange = { isActive ->
                    if (!isActive) {
                        searchQuery.value = ""
                    }
                },
                active = searchQuery.value.isNotEmpty()
            ),
            inputField = {
                predictions.forEach { prediction ->
                    ListItem(
                        headlineContent = {
                            Text(prediction.getPrimaryText(null).toString())
                        },
                        supportingContent = {
                            Text(prediction.getSecondaryText(null).toString())
                        },
                        modifier = Modifier.clickable {
                            viewModel.getPlaceDetails(prediction.placeId)
                            searchQuery.value = ""
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = SearchBarDefaults.Elevation
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
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
                is MapUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
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
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Select ${it.formattedAddress}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                is MapUiState.Error -> {
                    Text(
                        text = (uiState as MapUiState.Error).message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> Unit
            }
        }
    }
}