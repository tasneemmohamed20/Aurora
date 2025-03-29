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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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


@Composable
fun HomeScreen(
    forecastViewModel: ForecastViewModel,
    onNavigateToFav: () -> Unit

) {
//    val weatherState by currentWeatherViewModel.weatherState.collectAsState()
    val cityName by forecastViewModel.cityName.collectAsState()
    val forecastState by forecastViewModel.forecastState.collectAsState()

    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration) {
        forecastViewModel.onConfigurationChanged()
    }

    LaunchedEffect(Unit) {
//        currentWeatherViewModel.setupLocationUpdates()
        forecastViewModel.setupLocationUpdates()
    }

    val background = gradientBrush(isSystemInDarkTheme())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
//            .systemBarsPadding()
    ) {
        Column {
//            Spacer(modifier = Modifier.height(statusBarHeight))

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
                            onSettingsClick = {
                                // Handle settings navigation
                            },
                            onAlertsClick = {
                                // Handle alerts navigation
                            }
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
                when(forecastState) {
                    is ForecastUiState.Loading -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    is ForecastUiState.Success -> {
//                        val currentWeather = (weatherState as UiState.Success<CurrentResponse>).data
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
                                DailyForecast(forecastData)
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
                                            FeelsLike(feelsLike = (currentData.main?.feelsLike ?: 0.0) as Double)
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                            Humidity(humidity = currentData.main?.humidity ?: 0)
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

@Composable
fun CurrentWeatherContent(data: ListItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(52.dp))
        Text(
            text = "${(data.main?.temp as? Double)?.toInt()}°",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = data.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                ?: "Unknown",
            fontSize = 20.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

//@Composable
//fun FeelsLike(feelsLike: Double) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .background(
//                Color.White.copy(alpha = 0.1f),
//                shape = RoundedCornerShape(10.dp)
//            )
//            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp))
//            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
//    ) {
//        Row(
//            modifier = Modifier.wrapContentSize(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.feels_like_svg),
//                contentDescription = "Feels Like Icon",
//                modifier = Modifier.size(24.dp)
//            )
//            Text(
//                "Feels Like",
//                color = Color.White,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
//            )
//        }
//
//        Text(
//            text = "${feelsLike.toInt()}°",
//            color = Color.White,
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
//        )
//    }
//}
//
//@Composable
//fun Humidity(humidity: Int) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .background(
//                Color.White.copy(alpha = 0.1f),
//                shape = RoundedCornerShape(10.dp)
//            )
//            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp))
//            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
//    ) {
//        Row(
//            modifier = Modifier.wrapContentSize(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.humidity_svg),
//                contentDescription = "Humidity Icon",
//                modifier = Modifier.size(24.dp)
//            )
//            Text(
//                "Humidity",
//                color = Color.White,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
//            )
//        }
//
//        Text(
//            text = "$humidity%",
//            color = Color.White,
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
//        )
//    }
//}


@Composable
fun FeelsLike(feelsLike: Double) {
    MetricsCard(
        title = "Feels Like",
        value = "${feelsLike.toInt()}°",
        iconResId = R.drawable.feels_like_svg
    )
}

@Composable
fun Humidity(humidity: Int) {
    MetricsCard(
        title = "Humidity",
        value = "$humidity%",
        iconResId = R.drawable.humidity_svg
    )
}

//@Composable
//fun testUI(){
//    FeelsLike(17.0)
//}
//
//@Preview
//@Composable
//fun HomeScreenPreview() {
//testUI()
//}


