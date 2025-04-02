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

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel
){
    val context = LocalContext.current

    var expandedMenu by remember { mutableStateOf<String?>(null) }

    val temperatureUnits = listOf(
        "C", "F", "K")
    val windSpeedUnits = listOf("m/s", "Mp/h")
    val languageOptions = listOf("en", "ar")

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
                value = selectedTemperatureUnit,
                onSettingClick = { expandedMenu = "temperature" }
            )
            if (expandedMenu == "temperature") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = temperatureUnits,
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
                value = selectedSpeedUnit,
                onSettingClick = { expandedMenu = "windSpeed" }
            )
            if (expandedMenu == "windSpeed") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = windSpeedUnits,
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
                    options = languageOptions,
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
                value = selectedLanguage,
                onSettingClick = { expandedMenu = "language" }
            )
            if (expandedMenu == "language") {
                SettingsDropdownMenu(
                    expanded = true,
                    options = languageOptions,
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
