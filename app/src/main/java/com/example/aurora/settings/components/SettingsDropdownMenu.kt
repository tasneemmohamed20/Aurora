package com.example.aurora.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsDropdownMenu(
    expanded: Boolean,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,

) {
    var isDarkTheme = isSystemInDarkTheme()
    val menuColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
            .background(menuColor)
            .width(200.dp),
        offset = DpOffset(x = 16.dp, y = 0.dp)
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = option,
                        color = textColor,
                        fontSize = 16.sp
                    )
                },
                onClick = {
                    onOptionSelected(option)
                    onDismiss()
                }
            )
        }
    }
}