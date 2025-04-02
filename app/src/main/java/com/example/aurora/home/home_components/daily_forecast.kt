package com.example.aurora.home.home_components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurora.R
import com.example.aurora.data.model.forecast.ListItem
import com.example.aurora.utils.toDoubleOrZero
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun DailyForecast(hourlyData: List<ListItem>, context: Context) {
    val dailyForecasts = remember(hourlyData) {
        hourlyData.groupBy { item ->
            // Split by comma and get the date part
            item.dtTxt?.split(", ")?.getOrNull(1) ?: ""
        }.mapNotNull { (date, forecasts) ->
            try {
                // Calculate min/max temps for the day
                val temps = forecasts.mapNotNull { it.main?.temp?.toDoubleOrZero() }
                if (temps.isEmpty()) return@mapNotNull null

                // Parse the date and get day name
                val forecastDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(forecastDate)

                // Get first available weather icon for the day
                val icon = forecasts.firstOrNull()?.weather?.firstOrNull()?.icon ?: ""

                DailyForecastData(
                    dayName = dayName,
                    minTemp = temps.minOrNull()?.toInt() ?: 0,
                    maxTemp = temps.maxOrNull()?.toInt() ?: 0,
                    icon = icon
                )
            } catch (e: Exception) {
                Log.e("DailyForecast", "Error processing forecast: ${e.message}")
                null
            }
        }.distinctBy { it.dayName } // Remove duplicates
            .take(5) // Take only 5 days
            .mapIndexed { index, data ->
                if (index == 0) data.copy(dayName = context.resources.getString(R.string.today)) else data
            }
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
                LocalContext.current.resources.getString(R.string.DayForeCast),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 8.dp)
            )
        }

        HorizontalDivider(Modifier.padding(horizontal = 16.dp))

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            dailyForecasts.forEach { forecast ->
                DailyForecastRow(
                    day = forecast.dayName,
                    minTemp = forecast.minTemp,
                    maxTemp = forecast.maxTemp,
                    icon = forecast.icon
                )
            }
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