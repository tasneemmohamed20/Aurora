package com.example.aurora.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.aurora.R
import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.remote.RemoteDataSourceImp
import com.example.aurora.data.repo.WeatherRepositoryImp
import com.example.aurora.home.current_weather.viewmodel.CurrentWeatherViewModel
import com.example.aurora.home.current_weather.viewmodel.UiState
import com.example.aurora.home.hourly_daily_forecast.viewmodel.ForecastUiState
import com.example.aurora.home.hourly_daily_forecast.viewmodel.HourlyForecastData
import com.example.aurora.home.hourly_daily_forecast.viewmodel.HourlyForecastViewModel
import com.example.aurora.ui.theme.components.CustomAppBar
import com.example.aurora.ui.theme.components.MenuOptions
import com.example.aurora.ui.theme.gradientBrush
import com.example.aurora.utils.LocationHelper
import com.example.aurora.workers.WeatherWorkManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    context: Context = LocalContext.current,
    currentWeatherViewModel: CurrentWeatherViewModel = viewModel(
        factory = CurrentWeatherViewModel.WeatherViewModelFactory(
            WeatherRepositoryImp(RemoteDataSourceImp(), context),
            LocationHelper(context),
            WeatherWorkManager(context)
        )
    ),
    hourlyForecastViewModel: HourlyForecastViewModel = viewModel(
        factory = HourlyForecastViewModel.Factory(
            WeatherRepositoryImp(RemoteDataSourceImp(), context),
            LocationHelper(context)
        )
    )
) {
    val weatherState by currentWeatherViewModel.weatherState.collectAsState()
    val cityName by hourlyForecastViewModel.cityName.collectAsState()
    val forecastState by hourlyForecastViewModel.forecastState.collectAsState()

    LaunchedEffect(Unit) {
        currentWeatherViewModel.setupLocationUpdates()
        hourlyForecastViewModel.setupLocationUpdates()
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
                title = cityName ?: "Location",
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
                    IconButton(onClick = { /* handle settings click */ }) {
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
                when {
                    weatherState is UiState.Loading || forecastState is ForecastUiState.Loading -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    weatherState is UiState.Success && forecastState is ForecastUiState.Success -> {
                        val currentWeather = (weatherState as UiState.Success<CurrentResponse>).data
                        val forecastData = (forecastState as ForecastUiState.Success).data
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item { CurrentWeatherContent(currentWeather) }
                            item {
                                HourlyForecast(forecastData)
                                DailyForecast(forecastData)
                            }
                            item { WindData(
                                windSpeed = currentWeather.wind?.speed as? Double ?: 0.0,
                                windGust = currentWeather.wind?.gust as? Double ?: 0.0,
                                windDirection = currentWeather.wind?.deg?.toFloat() ?: 0f
                            ) }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        FeelsLike(feelsLike = currentWeather.main?.feelsLike ?: 0.0)
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        Humidity(humidity = currentWeather.main?.humidity ?: 0)
                                    }
                                }
                            }
                        }
                    }
                    weatherState is UiState.Error -> {
                        Text(
                            text = (weatherState as UiState.Error).message,
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    forecastState is ForecastUiState.Error -> {
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
fun CurrentWeatherContent(data: CurrentResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(52.dp))
//        Text(
//            text = cityName,
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.White
//        )
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

@Composable
private fun HourlyForecast(data: List<HourlyForecastData>) {
    val currentTimeMillis = remember { System.currentTimeMillis() }
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // Filter data for next 24 hours only
    val next24HoursForecast = remember(data) {
        data.filter { hourlyData ->
            try {
                val forecastDateTime = timeFormat.parse("${hourlyData.dt_txt} ${hourlyData.time}")?.time
                    ?: return@filter false

                // Convert "Now" to current time
                val actualTime = if (hourlyData.time == "Now") {
                    currentTimeMillis
                } else {
                    forecastDateTime
                }

                // Check if forecast is within next 24 hours
                actualTime >= currentTimeMillis &&
                        actualTime <= (currentTimeMillis + 24 * 60 * 60 * 1000)
            } catch (_: Exception) {
                false
            }
        }.take(8) // Typically 3-hour intervals, so 8 items = 24 hours
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp))
    ) {
        Text(
            "24-Hour Forecast",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider(Modifier.padding(horizontal = 16.dp))

        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(next24HoursForecast.size) { index ->
                HourlyForecastItem(next24HoursForecast[index])
            }
        }
    }
}

@Composable
private fun HourlyForecastItem(data: HourlyForecastData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = data.time,
            color = Color.White,
            fontSize = 14.sp
        )
        WeatherIcon(data.weatherIcon)
        Text(
            text = "${data.temperature}°",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun WeatherIcon(iconCode: String) {
    if (iconCode.isEmpty()) return

    val isDaytime = !isSystemInDarkTheme()
    val dayNight = if (isDaytime) "d" else "n"
    // Strip any existing d/n suffix and build the URL
    val baseCode = iconCode.take(2)
    val iconUrl = "https://openweathermap.org/img/wn/$baseCode$dayNight@2x.png"

    Log.d("WeatherIcon", "Original Icon Code: $iconCode")
    Log.d("WeatherIcon", "Base Code: $baseCode")
    Log.d("WeatherIcon", "Day/Night: $dayNight")
    Log.d("WeatherIcon", "Final URL: $iconUrl")

    GlideImage(
        model = iconUrl,
        contentDescription = "Weather Icon",
        modifier = Modifier.size(40.dp)
    ) {
        it.thumbnail()
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(80, 80) // Set explicit size
            .error(R.drawable.ic_launcher_foreground)
    }
}

@Composable
fun DailyForecast(data: List<HourlyForecastData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp))
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

        DailyForecastContent(data)
    }
}

@Composable
private fun DailyForecastContent(data: List<HourlyForecastData>) {
    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val dailyData = remember(data) {
        data.groupBy { it.dt_txt }
            .mapNotNull { (date, forecasts) ->
                try {
                    val temps = forecasts.map { it.temperature }
                    val isToday = date == today
                    val dayName = if (isToday) {
                        "Today"
                    } else {
                        SimpleDateFormat("EEE", Locale.getDefault()).format(
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!
                        )
                    }
                    // Get most frequent icon for the day
                    val mostFrequentIcon = forecasts
                        .groupBy { it.weatherIcon }
                        .maxByOrNull { it.value.size }
                        ?.key ?: "01d"

                    DailyForecastData(
                        dayName,
                        temps.minOrNull() ?: 0,
                        temps.maxOrNull() ?: 0,
                        mostFrequentIcon
                    )
                } catch (_: Exception) {
                    null
                }
            }.distinctBy { it.dayName }
            .take(5)
            .mapIndexed { index, data ->
                if (index == 0) data.copy(dayName = "Today") else data
            }
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        dailyData.forEach { dailyData ->
            DailyForecastRow(
                day = dailyData.dayName,
                minTemp = dailyData.minTemp,
                maxTemp = dailyData.maxTemp,
                icon = dailyData.icon
            )
        }
    }
}

data class DailyForecastData(
    val dayName: String,
    val minTemp: Int,
    val maxTemp: Int,
    val icon: String
)

@Composable
private fun DailyForecastRow(day: String, minTemp: Int, maxTemp: Int, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            day,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        WeatherIcon(icon)
        Text(
            "$maxTemp°",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 0.dp)
        )
        TemperatureRange(
            minTemp = minTemp,
            maxTemp = maxTemp,
            modifier = Modifier.weight(3f)
        )
        Text(
            "$minTemp°",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
    }
}

@Composable
private fun TemperatureRange(minTemp: Int, maxTemp: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF1C2938))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minValue = -10f
            val maxValue = 40f

            val rangeStart = ((minTemp - minValue) / (maxValue - minValue)) * size.width
            val rangeEnd = ((maxTemp - minValue) / (maxValue - minValue)) * size.width

            val brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF80DEEA),
                    Color(0xFFB2EBF2),
                    Color(0xFFFFEB3B),
                    Color(0xFFFFA726),
                    Color(0xFFFFA000)
                )
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(rangeStart, 0f),
                size = Size(rangeEnd - rangeStart, size.height),
                cornerRadius = CornerRadius(50f, 50f)
            )
        }
    }
}

@Composable
fun WindData(windSpeed: Double, windGust: Double, windDirection: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp))
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)

        ){
            Image(
                painter = painterResource(id = R.drawable.wind_svg),
                contentDescription = "Wind Icon",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "Wind",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }


        HorizontalDivider(
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.White.copy(alpha = 0.3f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WindDataRow("Wind", "${windSpeed.toInt()} mph")
                WindDataRow("Gusts", "${windGust.toInt()} mph")
                WindDataRow("Direction", "$windDirection° NNW")
            }
            Spacer(Modifier.width(16.dp))
            WindDirectionCompass(windDirection, windSpeed)
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
private fun WindDirectionCompass(windDirection: Float, windSpeed: Double) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.compass_ui_svg),
            contentDescription = "Compass Background",
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(id = R.drawable.compass_needle_svg),
            contentDescription = "Wind Direction",
            modifier = Modifier
                .fillMaxSize()
                .rotate(windDirection)
        )
        Column {
            Text(
                text = "mph",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "${windSpeed.toInt()}",
                color = Color.Black,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)

            )
        }

    }
}

@Composable
fun FeelsLike(feelsLike: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp))
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.feels_like_svg),
                contentDescription = "Feels Like Icon",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "Feels Like",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        Text(
            text = "${feelsLike.toInt()}°",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
    }
}

@Composable
fun Humidity(humidity: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(0.2.dp, Color.White, RoundedCornerShape(10.dp))
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.humidity_svg),
                contentDescription = "Humidity Icon",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "Humidity",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        Text(
            text = "$humidity%",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
//    FeelsLike()
//    Humidity()
}


