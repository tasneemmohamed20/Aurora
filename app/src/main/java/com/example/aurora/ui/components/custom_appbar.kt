package com.example.aurora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomAppBar(
    modifier: Modifier = Modifier,
    title: String,
    rightIcon: @Composable () -> Unit? = { null },
    leftIcon: (@Composable () -> Unit)? = null,

) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Transparent)
            .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leftIcon != null) {
                Box(modifier = Modifier.size(24.dp)) {
                    leftIcon()
                }
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Box(modifier = Modifier.size(24.dp)) {
                rightIcon()
            }
        }
    }
}

