package com.example.aurora.home.home_components


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurora.R

@Composable
fun MetricsCard(
    title: String,
    value: String,
    iconResId: Int,
    modifier: Modifier = Modifier
) {
    WeatherCard(
//        title = "",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()

                .padding(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start

        ) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = "$title Icon",
                    modifier = Modifier.size(24.dp)
                        .padding(bottom = 2.dp),
                    colorFilter = tint(Color.White)
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(Modifier.padding(horizontal = 4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun TestMetricsCard(){
    MetricsCard(
        title = "Temperature",
        value = "25°C",
        iconResId = R.drawable.feels_like_svg,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Preview
@Composable
fun PreviewMetricsCard() {
    TestMetricsCard()
}