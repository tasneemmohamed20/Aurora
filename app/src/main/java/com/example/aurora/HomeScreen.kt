package com.example.aurora

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aurora.Home.current_weather.ViewModel.CurrentWeatherViewModel
import com.example.aurora.Home.current_weather.ViewModel.UiState
import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.remote.RemoteDataSourceImp
import com.example.aurora.data.repo.WeatherRepositoryImp
import com.example.aurora.ui.theme.babyBlue
import com.example.aurora.ui.theme.babyPurple
import com.example.aurora.ui.theme.darkBabyBlue
import com.example.aurora.ui.theme.darkPurple
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun HomeScreen(
    viewModel: CurrentWeatherViewModel = viewModel(
        factory = CurrentWeatherViewModel.WeatherViewModelFactory(
            WeatherRepositoryImp(RemoteDataSourceImp()),
            context = LocalContext.current
        )
    ),
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val weatherState by viewModel.weatherState.collectAsState()
    val cityName by viewModel.cityName.collectAsState()

    val colors = if (isDarkTheme) {
        listOf(darkPurple, darkBabyBlue)
    } else {
        listOf(babyPurple, babyBlue)
    }

    val gradientBrush = Brush.linearGradient(
        colors = colors,
        start = Offset(Float.POSITIVE_INFINITY, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY),
        tileMode = TileMode.Decal
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { CurrentWeather(weatherState, cityName) }
        item { HourlyForecast() }
        item { DailyForecast() }
        item { WindData() }
    }
}

@Composable
fun CurrentWeather(weatherState: UiState<CurrentResponse>, cityName: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (weatherState) {
            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp),
                    color = Color.White
                )
            }
            is UiState.Success -> {
                val weather = weatherState.data
                Spacer(modifier = Modifier.height(52.dp))
                Text(
                    text = cityName ?: "Unknown Location",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${(weather.main?.temp as? Double)?.toInt()}°",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = weather.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            is UiState.Error -> {
                Text(
                    text = weatherState.message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun HourlyForecast() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Hourly Forecast",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp)
                .align(Alignment.Start)
        )

        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val hourlyData = listOf(
                "Now" to 15,
                "12AM" to 15,
                "1AM" to 14,
                "2AM" to 14,
                "3AM" to 13,
                "4AM" to 13,
                "Now" to 15,
                "12AM" to 15,
                "1AM" to 14,
                "2AM" to 14,
                "3AM" to 13,
                "4AM" to 13,
                "Now" to 15,
                "12AM" to 15,
                "1AM" to 14,
                "2AM" to 14,
                "3AM" to 13,
                "4AM" to 13
            )

            items(hourlyData.size) { index ->
                val (time, temp) = hourlyData[index]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(time, color = Color.White, fontSize = 14.sp)
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White)
                    Text("$temp°", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DailyForecast() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp)
            )
            Text(
                "5-Day Forecast",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 8.dp)
            )
        }

        HorizontalDivider(Modifier.padding(horizontal = 16.dp))

        val dailyData = remember {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val days = mutableListOf<Pair<String, Pair<Int, Int>>>()

            days.add("Today" to (15 to 29))

            repeat(4) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val dayName = dateFormat.format(calendar.time)
                days.add(dayName to (10 to 20))
            }
            days
        }

        // Use Column instead of LazyColumn to prevent nested scrolling issues
        Column {
            dailyData.forEach { (day, temps) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        day,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${temps.first}° - ${temps.second}°",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun WindData() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Text(
            "Wind & Pressure",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.White.copy(alpha = 0.3f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left column: Wind data
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WindDataRow(label = "Wind", value = "4 mph")
                WindDataRow(label = "Gusts", value = "4 mph")
                WindDataRow(label = "Direction", value = "345° NNW")
            }
            Spacer(Modifier.width(16.dp))
            // Right: Compass
            WindDirectionCompass(
                windDirection = 345f,
                modifier = Modifier.size(120.dp)
            )
        }
    }
}

@Composable
private fun WindDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun WindDirectionCompass(windDirection: Float, modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        // Compass background (Static)
        Image(
            painter = painterResource(id = R.drawable.compass_ui_svg),
            contentDescription = "Compass Background",
            modifier = Modifier.fillMaxSize()
        )

        // Rotating Needle
        Image(
            painter = painterResource(id = R.drawable.compass_needle_svg),
            contentDescription = "Wind Direction",
            modifier = Modifier
                .fillMaxSize()
                .rotate(windDirection)
        )
    }
}

@Preview
@Composable
fun WeatherScreenPreview() {
    HomeScreen()
}