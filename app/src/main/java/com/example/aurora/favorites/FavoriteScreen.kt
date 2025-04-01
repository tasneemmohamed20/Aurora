// File: app/src/main/java/com/example/aurora/favorites/FavoriteScreen.kt
package com.example.aurora.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurora.data.model.map.Location
import com.example.aurora.ui.components.CustomAppBar
import com.example.aurora.ui.components.CustomFab
import com.example.aurora.ui.components.SearchBarState
import com.example.aurora.ui.theme.gradientBrush
import com.example.aurora.utils.toDoubleOrZero

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: FavViewModel,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFavoriteClicked: (Location) -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value

    val queryState = remember { mutableStateOf("") }
    val activeState = remember { mutableStateOf(false) }


    SearchBarState(
        query = queryState.value,
        active = activeState.value,
        onQueryChange = { newQuery -> queryState.value = newQuery },
        onActiveChange = { active -> activeState.value = active }
    )

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush(isSystemInDarkTheme()))
        ) {
            CustomAppBar(
                title = "Favorites",
                leftIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )

            when (uiState) {
                is FavUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }

                is FavUiState.Success -> {
                    val forecasts = (uiState).forecasts

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val homeLocation = forecasts.find { it.isHome }
                        val otherLocations = forecasts.filterNot { it.isHome }

                        homeLocation?.let { forecast ->
                            item {
                                val firstItem = forecast.list?.firstOrNull()
                                val maxTemp =
                                    firstItem?.main?.tempMax?.toString()?.toDoubleOrNull()?.toInt()
                                        ?: forecast.list?.mapNotNull {
                                            it?.main?.tempMax?.toString()?.toDoubleOrNull()?.toInt()
                                        }?.maxOrNull() ?: 0
                                val minTemp =
                                    firstItem?.main?.tempMin?.toString()?.toDoubleOrNull()?.toInt()
                                        ?: forecast.list?.mapNotNull {
                                            it?.main?.tempMin?.toString()?.toDoubleOrNull()?.toInt()
                                        }?.minOrNull() ?: 0
                                val currentTemp =
                                    firstItem?.main?.temp?.toString()?.toDoubleOrNull() ?: 0.0

                                SwipeableFavoriteCard(
                                    city = forecast.city.name,
                                    maxTemp = "$maxTemp°",
                                    minTemp = "$minTemp°",
                                    currentTemp = currentTemp,
                                    isHome = true,
                                    onDelete = { viewModel.deleteFavorite(forecast) },
                                    onClick = {
                                        onFavoriteClicked(
                                            Location(
                                                forecast.city.coord?.lat.toDoubleOrZero(),
                                                forecast.city.coord?.lon.toDoubleOrZero()
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                                )
                            }
                        }

                        items(otherLocations) { forecast ->
                            val firstItem = forecast.list?.firstOrNull()
                            val maxTemp =
                                firstItem?.main?.tempMax?.toString()?.toDoubleOrNull()?.toInt()
                                    ?: forecast.list?.mapNotNull {
                                        it?.main?.tempMax?.toString()?.toDoubleOrNull()?.toInt()
                                    }?.maxOrNull() ?: 0
                            val minTemp =
                                firstItem?.main?.tempMin?.toString()?.toDoubleOrNull()?.toInt()
                                    ?: forecast.list?.mapNotNull {
                                        it?.main?.tempMin?.toString()?.toDoubleOrNull()?.toInt()
                                    }?.minOrNull() ?: 0
                            val currentTemp =
                                firstItem?.main?.temp?.toString()?.toDoubleOrNull() ?: 0.0

                            SwipeableFavoriteCard(
                                city = forecast.city.name,
                                maxTemp = "$maxTemp°",
                                minTemp = "$minTemp°",
                                currentTemp = currentTemp,
                                isHome = false,
                                onDelete = { viewModel.deleteFavorite(forecast) },
                                onClick = {
                                    onFavoriteClicked(
                                        Location(
                                            forecast.city.coord?.lat.toDoubleOrZero(),
                                            forecast.city.coord?.lon.toDoubleOrZero()
                                        )
                                    )
                                },
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }

                is FavUiState.Error -> {
                    Text(
                        text = (uiState).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        CustomFab(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            icon = Icons.Default.Add,
            onClick = onSearchClick
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableFavoriteCard(
    modifier: Modifier = Modifier,
    city: String,
    maxTemp: String,
    minTemp: String,
    currentTemp: Double,
    isHome: Boolean = false,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showHomeWarningDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue: SwipeToDismissBoxValue ->
            if (dismissValue != SwipeToDismissBoxValue.EndToStart) {
                if (isHome) {
                    showHomeWarningDialog = true
                } else {
                    showDeleteDialog = true
                }
            }
            false
        }
    )

    if (showHomeWarningDialog) {
        AlertDialog(
            onDismissRequest = { showHomeWarningDialog = false },
            title = { Text("Cannot Delete Home") },
            text = { Text("The home location cannot be deleted. Please set another location as home first if you want to remove this one.") },
            confirmButton = {
                Button(
                    onClick = { showHomeWarningDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Location") },
            text = { Text("Are you sure you want to delete this location from favorites?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {}
    ) {
        FavoriteCard(
            city = city,
            maxTemp = maxTemp,
            minTemp = minTemp,
            isHome = isHome,
            currentTemp = currentTemp,
            onClick = onClick,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteCard(
    modifier: Modifier = Modifier,
    city: String,
    maxTemp: String,
    minTemp: String,
    currentTemp: Double,
    onClick: () -> Unit,
    isHome: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(80.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = city,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isHome) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home Location",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = "$maxTemp /",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = minTemp,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            var castedCurrentTemp = currentTemp.toInt()
            Text(
                text = "$castedCurrentTemp°",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}