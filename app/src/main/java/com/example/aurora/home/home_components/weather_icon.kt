package com.example.aurora.home.home_components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.aurora.R

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WeatherIcon(iconCode: String) {
    if (iconCode.isEmpty()) return

    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"

    GlideImage(
        model = iconUrl,
        contentDescription = "Weather Icon",
        modifier = Modifier.size(40.dp)
    ) {
        it.thumbnail()
            .override(80, 80)
            .error(R.drawable.ic_launcher_foreground)
    }
}