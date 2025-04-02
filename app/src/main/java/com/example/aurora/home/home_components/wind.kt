package com.example.aurora.home.home_components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurora.R
import com.example.aurora.settings.SettingsManager

@Composable
fun WindData(windSpeed: Double, windGust: Double, windDirection: Float) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
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
                LocalContext.current.resources.getString(R.string.Wind),
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WindDataRow(LocalContext.current.resources.getString(R.string.Wind), "${windSpeed.toInt()} ${settingsManager.getSpeedUnit()}")
                WindDataRow(LocalContext.current.resources.getString(R.string.gust), "${windGust.toInt()} ${settingsManager.getSpeedUnit()}")
                WindDataRow(LocalContext.current.resources.getString(R.string.direction), "$windDirection° NNW")
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
            fontSize = 20.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WindDirectionCompass(windDirection: Float, windSpeed: Double) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
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
                text = settingsManager.getSpeedUnit(),
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