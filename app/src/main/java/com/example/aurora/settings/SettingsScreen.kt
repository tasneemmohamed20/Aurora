package com.example.aurora.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aurora.settings.components.SettingRow
import com.example.aurora.settings.components.SettingsDropdownMenu
import com.example.aurora.ui.components.CustomAppBar
import com.example.aurora.ui.theme.gradientBrush

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
){
    var expandedMenu by remember { mutableStateOf<String?>(null) }
    val temperatureUnits = listOf("C", "F", "K")
    val windSpeedUnits = listOf("Km/h", "Mp/h")
    val locationOptions = listOf("GPS", "Manual")
    val languageOptions = listOf("en", "ar")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush(isSystemInDarkTheme()))
    ) {
        CustomAppBar(
            title = "Settings",
            leftIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        )

        Spacer( modifier = Modifier.size(16.dp))
        Box{
            SettingRow(
                modifier = Modifier.fillMaxWidth(),
                settingName = "Temperature Unit",
                value = "C",
                onSettingClick = { expandedMenu = "temperature" },
            )
            if (expandedMenu == "temperature") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = temperatureUnits,
                    onOptionSelected = { /* Handle selection */ },
                    onDismiss = { expandedMenu = null },
//                    anchor = {}
                )
            }
        }
        Spacer( modifier = Modifier.size(16.dp))

        Box{
            SettingRow(
                modifier = Modifier.fillMaxWidth(),
                settingName = "Wind Speed Unit",
                value = "Km/h",
                onSettingClick = { expandedMenu = "windSpeed" }
            )
            if (expandedMenu == "windSpeed") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = windSpeedUnits,
                    onOptionSelected = { /* Handle selection */ },
                    onDismiss = { expandedMenu = null },
                )
            }
        }
        Spacer( modifier = Modifier.size(16.dp))


        Box{
            SettingRow(
                modifier = Modifier.fillMaxWidth(),
                settingName = "Location",
                value = "GPS",
                onSettingClick = { expandedMenu = "location" }
            )
            if (expandedMenu == "location") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = locationOptions,
                    onOptionSelected = { /* Handle selection */ },
                    onDismiss = { expandedMenu = null },
                )
            }

        }
        Spacer( modifier = Modifier.size(16.dp))

        Box{
            SettingRow(
                modifier = Modifier.fillMaxWidth(),
                settingName = "Language",
                value = "en",
                onSettingClick = { expandedMenu = "language" }
            )
            if (expandedMenu == "language") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = languageOptions,
                    onOptionSelected = { /* Handle selection */ },
                    onDismiss = { expandedMenu = null },

                    )
            }
        }
    }

}



@Preview
@Composable
fun TestUI(){
    SettingsScreen {

    }
}