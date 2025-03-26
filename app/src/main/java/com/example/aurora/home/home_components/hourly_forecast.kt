package com.example.aurora.home.home_components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.aurora.R
import com.example.aurora.data.model.hourly_daily.ListItem
import com.example.aurora.utils.toIntOrZero
import java.text.SimpleDateFormat
import java.util.Locale



@Composable
fun HourlyForecast(hourlyData: List<ListItem>) {
    val currentTimeMillis = remember { System.currentTimeMillis() }
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Filter data for next 24 hours only
    val next24HoursForecast = remember(hourlyData) {
        hourlyData.filter { hourlyData ->
            try {
                val dtTxt = hourlyData.dtTxt ?: return@filter false
                if (dtTxt.startsWith("Now")) return@filter true

                // Parse the time and date parts
                val parts = dtTxt.split(", ")
                if (parts.size != 2) return@filter false

                val time = parts[0]
                val date = parts[1]

                val dateTimeStr = "$date $time:00"
                val forecastDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(dateTimeStr)?.time ?: return@filter false

                // Check if forecast is within next 24 hours
                forecastDateTime >= currentTimeMillis &&
                        forecastDateTime <= (currentTimeMillis + 24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                Log.e("HourlyForecast", "Error parsing date: ${e.message}")
                false
            }
        }.take(8).also {
            Log.d("HourlyForecast", "Filtered forecast size: ${it.size}")
        }
    }

    // Only show if we have forecast data
    if (next24HoursForecast.isNotEmpty()) {
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
}

@Composable
private fun HourlyForecastItem(data: ListItem) {
    val timeText = remember(data.dtTxt) {
        data.dtTxt?.split(", ")?.getOrNull(0) ?: "N/A"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = timeText,
            color = Color.White,
            fontSize = 14.sp
        )
        WeatherIcon(data.weather?.firstOrNull()?.icon ?: "")
        Text(
            text = "${data.main?.temp?.toIntOrZero()}°",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}



