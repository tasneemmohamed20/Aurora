package com.example.aurora.home.home_components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurora.R
import com.example.aurora.data.model.forecast.ListItem
import com.example.aurora.utils.toIntOrZero
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.format
import kotlin.text.get
import kotlin.text.toLong


@Composable
fun HourlyForecast(hourlyData: List<ListItem>) {
    val currentTimeMillis = remember { System.currentTimeMillis() }

    // Filter data for next 24 hours only
    val next24HoursForecast = remember(hourlyData) {
        hourlyData.filter { hourlyData ->
            try {
                val timestamp = hourlyData.dt?.toLong()?.times(1000) ?: return@filter false

                // Check if forecast is within next 24 hours
                timestamp >= currentTimeMillis &&
                        timestamp <= (currentTimeMillis + 24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                Log.e("HourlyForecast", "Error parsing date: ${e.message}")
                false
            }
        }.take(8)
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
                LocalContext.current.resources.getString(R.string.HourForeCast),
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
    val timeText = remember(data.dt) {
        val timestamp = data.dt?.toLong()?.times(1000) ?: return@remember "N/A"
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
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
