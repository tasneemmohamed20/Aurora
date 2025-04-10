package com.example.aurora.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurora.R
import com.example.aurora.data.model.forecast.ListItem
import com.example.aurora.home.home_components.DailyForecast
import com.example.aurora.home.home_components.HourlyForecast
import com.example.aurora.home.home_components.MetricsCard
import com.example.aurora.home.home_components.WindData
import com.example.aurora.ui.components.CustomAppBar
import com.example.aurora.ui.components.MenuOptions
import com.example.aurora.ui.theme.gradientBrush
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    forecastViewModel: ForecastViewModel,
    onNavigateToFav: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAlerts: () -> Unit,
) {
    LaunchedEffect(Unit) {
        forecastViewModel.resetToCurrentLocation()
    }
    val cityName by forecastViewModel.cityName.collectAsState()
    val forecastState by forecastViewModel.forecastState.collectAsState()
    val showHomeDialog by forecastViewModel.homeDialogVisible.collectAsState()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    if (showHomeDialog) {
        AlertDialog(
            onDismissRequest = { forecastViewModel.dismissHomeDialog() },
            title = { Text(context.resources.getString(R.string.setHomeLocation)) },
            text = { Text(context.resources.getString(R.string.setHomeLocation_msg)) },
            confirmButton = {
                Button(
                    onClick = { forecastViewModel.confirmHomeLocation() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(context.resources.getString(R.string.yes))
                }
            },
            dismissButton = {
                Button(
                    onClick = { forecastViewModel.dismissHomeDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(context.resources.getString(R.string.no))
                }
            }
        )
    }
    fun handleRefresh() {
        coroutineScope.launch {
            isRefreshing = true
            forecastViewModel.refresh()
            isRefreshing = false
        }
    }

    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration) {
        forecastViewModel.onConfigurationChanged()
    }

    val background = gradientBrush(isSystemInDarkTheme())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { handleRefresh() },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        ){
            Column {
                CustomAppBar(
                    title = cityName ?: "",
                    rightIcon = {
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                            MenuOptions(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                onSettingsClick = onNavigateToSettings,
                                onAlertsClick = onNavigateToAlerts,
                                context = context
                            )
                        }
                    },
                    leftIcon = {
                        IconButton(onClick = onNavigateToFav) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (forecastState) {
                        is ForecastUiState.Loading -> {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        is ForecastUiState.Success -> {
                            val forecastData = (forecastState as ForecastUiState.Success).data
                            val currentData = forecastData.first()
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item { CurrentWeatherContent(currentData) }
                                item {
                                    HourlyForecast(forecastData)
                                    DailyForecast(forecastData, context)
                                }
                                item {
                                    WindData(
                                        windSpeed = currentData.wind?.speed as? Double ?: 0.0,
                                        windGust = currentData.wind?.gust as? Double ?: 0.0,
                                        windDirection = currentData.wind?.deg?.toFloat() ?: 0f
                                    )
                                }
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            FeelsLike(
                                                feelsLike = (currentData.main?.feelsLike
                                                    ?: 0.0) as Double
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            Humidity(humidity = currentData.main?.humidity ?: 0)
                                        }
                                    }
                                }
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            Clouds(clouds = currentData.clouds?.all ?: 0)
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            Pressure(pressure = currentData.main?.pressure ?: 0)
                                        }
                                    }
                                }
                            }
                        }

                        is ForecastUiState.Error -> {
                            Text(
                                text = (forecastState as ForecastUiState.Error).message,
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun CurrentWeatherContent(data: ListItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "${(data.main?.temp as? Double)?.toInt()}°",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = data.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                ?: LocalContext.current.resources.getString(R.string.unknown),
            fontSize = 20.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentDateTime = remember {
                val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val now = Date()
                Pair(dateFormat.format(now), timeFormat.format(now))
            }

            Text(
                text = currentDateTime.first, // Date
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = " • ", // Separator
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                text = currentDateTime.second, // Time
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}


@Composable
fun FeelsLike(feelsLike: Double) {
    MetricsCard(
        title = LocalContext.current.resources.getString(R.string.FeelsLike),
        value = "${feelsLike.toInt()}°",
        iconResId = R.drawable.feels_like_svg
    )
}

@Composable
fun Humidity(humidity: Int) {
    MetricsCard(
        title = LocalContext.current.resources.getString(R.string.Humidity),
        value = "$humidity%",
        iconResId = R.drawable.humidity_svg
    )
}

@Composable
fun Pressure(pressure: Int) {
    MetricsCard(
        title = LocalContext.current.resources.getString(R.string.Pressure),
        value = "$pressure hPa",
        iconResId = R.drawable.pressure_svg
    )
}

@Composable
fun Clouds(clouds: Int) {
    MetricsCard(
        title = LocalContext.current.resources.getString(R.string.Clouds),
        value = "$clouds%",
        iconResId = R.drawable.cloud_svg
    )
}



