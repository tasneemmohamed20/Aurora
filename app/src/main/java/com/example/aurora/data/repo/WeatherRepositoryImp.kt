// File: `app/src/main/java/com/example/aurora/data/repo/WeatherRepositoryImp.kt`
package com.example.aurora.data.repo

import android.content.Context
import android.util.Log
import com.example.aurora.R
import com.example.aurora.data.local.LocalDataSource
import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.remote.RemoteDataSource
import com.example.aurora.settings.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.toString

class WeatherRepositoryImp private constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val context: Context,
    private val settingsManager: SettingsManager
) : WeatherRepository {

    private val apiKey = context.getString(R.string.weather_api_key)
    private val geocodingApiKey = context.getString(R.string.MAPS_API_KEY)

//    private val currentLocale: String
//        get() = context.resources.configuration.locales[0].toString().substring(0, 2)

    // Remote
    override suspend fun getForecast(
        latitude: Double,
        longitude: Double
    ): Flow<ForecastResponse> {
//        Log.d("WeatherRepositoryImp", "Locale: $currentLocale")
        return remoteDataSource.getHourlyDailyForecast(
            apiKey = apiKey,
            lat = latitude,
            lon = longitude,
            language = settingsManager.language,
            units = settingsManager.temperatureUnit
        ).map { response ->
            // Preserve isHome flag when updating existing forecast
            localDataSource.getAllForecasts().firstOrNull()?.let { forecasts ->
                val existingForecast = forecasts.find { it.city.name == response.city.name }
                response.copy(isHome = existingForecast?.isHome == true)
            } ?: response
        }
    }

    override suspend fun getAddressFromGeocoding(
        latlng: String,
        apiKey: String
    ): Flow<String> {
        return remoteDataSource.getAddressFromGeocoding(latlng, geocodingApiKey)
    }


    //Local - Forecast
    override suspend fun getAllForecasts(): Flow<List<ForecastResponse>> {
        return localDataSource.getAllForecasts()
    }

    override suspend fun insertForecast(forecast: ForecastResponse): Long {
        val existingForecasts = localDataSource.getAllForecasts().firstOrNull() ?: emptyList()

        // Delete all forecasts with same coordinates
        existingForecasts.filter { existing ->
            val existingLat = existing.city.coord.lat.toString().toDoubleOrNull()
            val existingLon = existing.city.coord.lon.toString().toDoubleOrNull()
            val newLat = forecast.city.coord.lat.toString().toDoubleOrNull()
            val newLon = forecast.city.coord.lon.toString().toDoubleOrNull()

            existingLat != null && existingLon != null &&
                    newLat != null && newLon != null &&
                    existingLat == newLat && existingLon == newLon
        }.forEach { existingForecast ->
            localDataSource.deleteForecast(existingForecast)
        }

        // Create forecast copy with correct isHome flag
        val forecastToInsert = when {
            // If explicitly setting as home or if first forecast
            forecast.isHome || existingForecasts.isEmpty() -> {
                forecast.copy(isHome = true)
            }
            // If existing forecast was home, preserve home status
            existingForecasts.any { it.isHome &&
                    it.city.coord.lat.toString().toDoubleOrNull() == forecast.city.coord.lat.toString().toDoubleOrNull() &&
                    it.city.coord.lon.toString().toDoubleOrNull() == forecast.city.coord.lon.toString().toDoubleOrNull() } -> {
                forecast.copy(isHome = true)
            }
            // New forecast, not home
            else -> forecast.copy(isHome = false)
        }

        // Insert the new forecast
        return localDataSource.insertForecast(forecastToInsert)
    }

    override suspend fun deleteForecast(forecast: ForecastResponse): Int {
        return localDataSource.deleteForecast(forecast)
    }

    override suspend fun getForecastByCityName(cityName: String): Flow<ForecastResponse?> {
        return localDataSource.getForecastByCityName(cityName).map { forecast ->
            // Preserve isHome flag when fetching by city name
            forecast?.let {
                val existingForecasts = localDataSource.getAllForecasts().firstOrNull() ?: emptyList()
                val existingForecast = existingForecasts.find { it.city.name == it.city.name }
                it.copy(isHome = existingForecast?.isHome == true)
            }
        }
    }

    // Local - Weather Alert Settings

    override suspend fun getAllAlerts(): Flow<List<WeatherAlertSettings>> {
        return localDataSource.getAllAlerts()
    }
    override suspend fun insertAlert(alert: WeatherAlertSettings): Long {
        return localDataSource.insertAlert(alert)
    }
    override suspend fun deleteAlert(alert: WeatherAlertSettings): Int {
        return localDataSource.deleteAlert(alert)
    }

    companion object {
        private var instance: WeatherRepositoryImp? = null
        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource,
            context: Context,
            settingsManager: SettingsManager
        ): WeatherRepositoryImp {
            return instance ?: synchronized(this) {
                val temp = WeatherRepositoryImp(
                    remoteDataSource,
                    localDataSource,
                    context,
                    settingsManager
                )
                instance = temp
                temp
            }
        }
    }
}