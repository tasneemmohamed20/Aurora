package com.example.aurora.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aurora.R
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
    source: String,
    viewModel : MapsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLocation by viewModel.location.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    val context = LocalContext.current

    val markerState = remember {
        MarkerState(LatLng(location.lat, location.lng))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerState.position, 10f)
    }

    val predictions by viewModel.predictions.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()

    // Remove auto-selection for settings flow.
    if (source != "settings") {
        LaunchedEffect(source) {
            viewModel.setSource(source)
        }
    } else {
        // In settings flow, also set the source so that later on the confirmation uses home action.
        LaunchedEffect(Unit) {
            viewModel.setSource("settings")
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            val newLatLng = LatLng(loc.lat, loc.lng)
            markerState.position = newLatLng
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 10f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    markerState.position = latLng
                    viewModel.updateLocation(Location(latLng.latitude, latLng.longitude))
                    viewModel.openDialog()
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
                    // Optionally display address information.
                }
                is MapUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (uiState as MapUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.retryLocationUpdate() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(context.resources.getString(R.string.retry))
                        }
                    }
                }
                else -> Unit
            }
        }

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
            searchResults = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(predictions.size) { index ->
                            val prediction = predictions[index]
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.getPlaceDetails(prediction.placeId)
                                        searchQuery.value = ""
                                    }
                                    .padding(horizontal = 16.dp),
                                headlineContent = {
                                    Text(
                                        text = prediction.getPrimaryText(null).toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = prediction.getSecondaryText(null).toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }
            }
        )

        // Updated dialog: confirm button performs home update if source is "settings"
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = {
                    Text(
                        text = if (source == "settings")
                            context.resources.getString(R.string.setHomeLocation)
                        else
                            context.resources.getString(R.string.addToFav)
                    )
                },
                text = {
                    Text(
                        text = if (source == "settings")
                            context.resources.getString(R.string.setHomeLocation_msg)
                        else
                            context.resources.getString(R.string.addToFavMsg)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (source == "settings") {
                                viewModel.handleDialogConfirm {  // Pass lambda function as expected
                                    currentLocation?.let { location ->
                                        onLocationSelected(location)
                                    }
                                }  // calls addToHomeLocation()
                            } else {
                                viewModel.handleDialogConfirm {  // Pass lambda function as expected
                                    currentLocation?.let { location ->
                                        onLocationSelected(location)
                                    }
                                }
                            }
//                            currentLocation?.let { onLocationSelected(it) }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (source == "settings")
                            context.resources.getString(R.string.yes)
                        else
                            context.resources.getString(R.string.yes))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { viewModel.dismissDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(context.resources.getString(R.string.no))
                    }
                }
            )
        }
    }
}