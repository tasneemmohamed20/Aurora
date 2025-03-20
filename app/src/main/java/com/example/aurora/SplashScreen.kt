package com.example.aurora

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.aurora.ui.theme.AuroraTheme
import com.example.aurora.ui.theme.babyBlue
import com.example.aurora.ui.theme.babyPurple
import com.example.aurora.ui.theme.darkBabyBlue
import com.example.aurora.ui.theme.darkPurple
import kotlinx.coroutines.delay

@Composable
fun SplashScreenUI(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onNavigateToHome: () -> Unit = {}
    ) {
    val colors = if (isDarkTheme) {
        listOf(darkPurple, darkBabyBlue)
    } else {
        listOf(babyPurple, babyBlue)
    }

    val lottieSize = remember { Animatable(500f) }
    val imageSize = remember { Animatable(80f) }
    val imageAlpha = remember { Animatable(0f) }
    val spacing = remember { Animatable(16f) }

    LaunchedEffect(Unit) {
        delay(500)

        // Shrink Lottie
        lottieSize.animateTo(
            targetValue = 150f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )

        // Fade in and grow image
        imageAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
        imageSize.animateTo(
            targetValue = 120f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
        spacing.animateTo(
            targetValue = -40f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )

        delay(1000)
        onNavigateToHome()
    }

    val gradientBrush = Brush.linearGradient(
        colors = colors,
        start = Offset(Float.POSITIVE_INFINITY, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY),
        tileMode = TileMode.Decal
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.value.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LottieAnimation(
                composition = rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(R.raw.aurora_animation)
                ).value,
                modifier = Modifier.size(lottieSize.value.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.auroralogo),
                contentDescription = "Aurora Logo",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .size(imageSize.value.dp)
                    .graphicsLayer(alpha = imageAlpha.value)
                    .padding(top = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    AuroraTheme {
        SplashScreenUI()
    }
}