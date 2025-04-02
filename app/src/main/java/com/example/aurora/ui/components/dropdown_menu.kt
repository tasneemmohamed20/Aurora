package com.example.aurora.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aurora.R

@Composable
fun MenuOptions(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSettingsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    context: Context
) {

    var isDark = isSystemInDarkTheme()
    var bgColor = if (isDark) {
        Color.Black
    } else {
        Color.White
    }

    var textColor = if (isDark) {
        Color.White
    } else {
        Color.Black
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.width(180.dp)
            .background(bgColor)
    ) {
        DropdownMenuItem(
            text = { Text(context.resources.getString(R.string.settings), color = textColor) },
            onClick = {
                onSettingsClick()
                onDismissRequest()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = textColor
                )
            }
        )
        DropdownMenuItem(
            text = { Text(context.resources.getString(R.string.notifications), color = textColor) },
            onClick = {
                onAlertsClick()
                onDismissRequest()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alerts",
                    tint = textColor
                )
            }
        )
    }
}