package com.example.aurora.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode


// day
val babyBlue = Color(0xFFA8C8ED)
val babyPurple = Color(0xFF7673DC)

// night
val darkPurple = Color(0xFF48355B)
val darkBabyBlue = Color(0xFF91BEF3)

val darkGradientColors = listOf(darkPurple, darkBabyBlue)
val lightGradientColors = listOf(babyPurple, babyBlue)

fun gradientBrush(isDarkTheme: Boolean): Brush {
    val colors = if (isDarkTheme) {
        darkGradientColors
    } else {
        lightGradientColors
    }
    return Brush.linearGradient(
        colors = colors,
        start = Offset(Float.POSITIVE_INFINITY, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY),
        tileMode = TileMode.Decal
    )
}