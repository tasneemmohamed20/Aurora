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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aurora.R
import com.example.aurora.settings.components.SettingRow
import com.example.aurora.settings.components.SettingsDropdownMenu
import com.example.aurora.ui.components.CustomAppBar
import com.example.aurora.ui.theme.gradientBrush
//1 //2 //3 // 4
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel
){
    val context = LocalContext.current

    var expandedMenu by remember { mutableStateOf<String?>(null) }

    val temperatureUnits = listOf(
        Pair("C", context.getString(R.string.celsius)),
        Pair("F", context.getString(R.string.fahrenheit)),
        Pair("K", context.getString(R.string.kelvin))
    )

    val windSpeedUnits = listOf(
        Pair("m/s", context.getString(R.string.meters_per_second)),
        Pair("Mp/h", context.getString(R.string.miles_per_hour))
    )

    val languageOptions = listOf(
        Pair("en", context.getString(R.string.english)),
        Pair("ar", context.getString(R.string.arabic))
    )

    val locationOptions = listOf(
        Pair("GPS", context.getString(R.string.gps)),
        Pair("Manual", context.getString(R.string.manual))
    )

    val selectedTemperatureUnit by viewModel.selectedTemperatureUnit.collectAsState()
    val selectedSpeedUnit by viewModel.selectedSpeedUnit.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush(isSystemInDarkTheme()))
    ) {
        CustomAppBar(
            title = context.resources.getString(R.string.settings),
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
        Box {
            SettingRow(
                modifier = Modifier.fillMaxWidth(),
                settingName = context.resources.getString(R.string.temperatureunit),
                value = temperatureUnits.find { it.first == selectedTemperatureUnit }?.second
                    ?: selectedTemperatureUnit,
                onSettingClick = { expandedMenu = "temperature" }
            )
            if (expandedMenu == "temperature") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = temperatureUnits.map { it.second },
                    onOptionSelected = { viewModel.updateTemperatureUnit(it) },
                    onDismiss = { expandedMenu = null }
                )
            }
        }
        Spacer( modifier = Modifier.size(16.dp))

        Box {
            SettingRow(
                modifier = Modifier.fillMaxWidth(),
                settingName = context.resources.getString(R.string.wind_speedunit),
                value = windSpeedUnits.find { it.first == selectedSpeedUnit }?.second
                ?: selectedSpeedUnit,
                onSettingClick = { expandedMenu = "windSpeed" }
            )
            if (expandedMenu == "windSpeed") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = windSpeedUnits.map { it.second },
                    onOptionSelected = { unit ->
                        when (unit) {
                            "m/s" -> viewModel.updateTemperatureUnit("C")
                            "Mp/h" -> viewModel.updateTemperatureUnit("F")
                        }
                    },
                    onDismiss = { expandedMenu = null }
                )
            }
        }
        Spacer( modifier = Modifier.size(16.dp))


        Box{
            SettingRow(
                modifier = Modifier.fillMaxWidth(),
                settingName = context.resources.getString(R.string.location),
                value = "GPS",
                onSettingClick = { expandedMenu = "location" }
            )
            if (expandedMenu == "location") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = locationOptions.map { it.second },
                    onOptionSelected = {
                        viewModel.updateLanguage(it)
                        // Let configuration changes handle the UI update
                    },
                    onDismiss = { expandedMenu = null }
                )
            }

        }
        Spacer( modifier = Modifier.size(16.dp))

        Box{
            SettingRow(
                modifier = Modifier.fillMaxWidth(),
                settingName = context.resources.getString(R.string.language),
                value = languageOptions.find { it.first == selectedLanguage }?.second
                    ?: selectedLanguage,
                onSettingClick = { expandedMenu = "language" }
            )
            if (expandedMenu == "language") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = languageOptions.map { it.second },
                    onOptionSelected = {
                        viewModel.updateLanguage(it)
                        expandedMenu = null
                    },
                    onDismiss = { expandedMenu = null }
                )
            }
        }
    }

}
