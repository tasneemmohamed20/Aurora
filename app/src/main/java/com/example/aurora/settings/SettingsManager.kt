package com.example.aurora.settings

import android.content.Context

class SettingsManager(private val context: Context) {
    private val prefs by lazy {
        context.applicationContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        const val PREF_TEMPERATURE_UNIT = "temperature_unit"
        const val PREF_LANGUAGE = "language"

        // API units parameter values
        const val UNIT_METRIC = "metric"     // For Celsius
        const val UNIT_IMPERIAL = "imperial" // For Fahrenheit
        const val UNIT_STANDARD = "" // For Kelvin

        // API supported languages
        const val LANG_ENGLISH = "en"
        const val LANG_ARABIC = "ar"
    }



    // Temperature unit with proper API parameter values
    var temperatureUnit: String
        get() = prefs.getString(PREF_TEMPERATURE_UNIT, UNIT_METRIC) ?: UNIT_METRIC
        set(value) {
            val validUnit = when (value) {
                "C" -> UNIT_METRIC
                "F" -> UNIT_IMPERIAL
                "K" -> UNIT_STANDARD
                else -> UNIT_METRIC
            }
            prefs.edit().putString(PREF_TEMPERATURE_UNIT, validUnit).apply()
        }

    // Display value for UI
    fun getDisplayTemperatureUnit(): String {
        return when (temperatureUnit) {
            UNIT_METRIC -> "C"
            UNIT_IMPERIAL -> "F"
            UNIT_STANDARD -> "K"
            else -> "C"
        }
    }

    fun getSpeedUnit(): String {
        return when (temperatureUnit) {
            UNIT_METRIC -> "m/s"
            UNIT_IMPERIAL -> "Mp/h"
            else -> "m/s"
        }
    }

    // Helper function to get both units for display
    fun getDisplayUnits(): Pair<String, String> {
        return when (temperatureUnit) {
            UNIT_METRIC -> Pair("C", "m/s")
            UNIT_IMPERIAL -> Pair("F", "Mp/h")
            UNIT_STANDARD -> Pair("K", "m/s")
            else -> Pair("C", "m/s")
        }
    }

    private val currentLocale: String
        get() = context.resources.configuration.locales[0].toString().substring(0, 2)
    // Language with API's supported codes
    var language: String
        get() = prefs.getString(PREF_LANGUAGE, LANG_ENGLISH) ?: currentLocale
        set(value) {
            val validLang = when (value.lowercase()) {
                "ar", LANG_ARABIC -> LANG_ARABIC
                else -> LANG_ENGLISH
            }
            prefs.edit().putString(PREF_LANGUAGE, validLang).apply()
        }
}