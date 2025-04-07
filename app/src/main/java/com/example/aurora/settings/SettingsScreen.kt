package com.example.aurora.settings

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
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
    onOpenMap: (source: String) -> Unit,
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
        Pair("gps", context.getString(R.string.gps)),
        Pair("manual", context.getString(R.string.manual))
    )

    val selectedTemperatureUnit by viewModel.selectedTemperatureUnit.collectAsState()
    val selectedSpeedUnit by viewModel.selectedSpeedUnit.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedLocationMode by viewModel.selectedLocationMode.collectAsState()
    val openMap by viewModel.openMap.collectAsState()

    LaunchedEffect(openMap) {
        if (openMap) {
            onOpenMap("settings")
            viewModel.onMapOpened()
        }
    }

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
                    onOptionSelected = { viewModel.updateTemperatureUnit(it.substring(0, 1))
                        Log.d("SettingsScreen", "Selected temperature unit: $it")},
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
                value = locationOptions.find { it.first == selectedLocationMode }?.second
                    ?: selectedLocationMode,
                onSettingClick = { expandedMenu = "location" }
            )
            if (expandedMenu == "location") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = locationOptions.map { it.second },
                    onOptionSelected = { mode ->
                        when (mode) {
                            context.getString(R.string.gps) -> viewModel.updateLocationMode(SettingsManager.MODE_GPS)
                            context.getString(R.string.manual) -> viewModel.updateLocationMode(SettingsManager.MODE_MANUAL)
                        }
                        expandedMenu = null
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
                        viewModel.updateLanguage(it.substring(0, 2))
                        expandedMenu = null
                        Log.d("SettingsScreen", "Selected language: $it")
                    },
                    onDismiss = { expandedMenu = null }
                )
            }
        }
    }

}
