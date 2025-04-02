package com.example.aurora.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurora.R

@Composable
fun SettingRow(
    modifier: Modifier,
    settingName: String,
    value: String,
    onSettingClick: () -> Unit = {}
){

    var rowHeight by remember { mutableStateOf(0) }
    LocalDensity.current

    Row(
        modifier = modifier
            .clickable(onClick = onSettingClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .onSizeChanged { rowHeight = it.height },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = settingName,
            fontSize = 16.sp,
            color = Color.White
        )
        Row(
            modifier = Modifier
                .weight(2f)
                .padding(end = 16.dp)
                .wrapContentWidth(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Image(
                painter = painterResource(R.drawable.expand_list_svg),
                contentDescription = "Icon",
                modifier = Modifier
                    .size(20.dp)
                    .padding(bottom = 2.dp, start = 4.dp),
                colorFilter = tint(Color.White.copy(alpha = 0.7f))
            )
        }
    }
}