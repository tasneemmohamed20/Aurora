// File: `app/src/main/java/com/example/aurora/data/repo/WeatherRepositoryImp.kt`
package com.example.aurora.data.repo

import android.util.Log
import com.example.aurora.data.local.LocalDataSource
import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.remote.RemoteDataSource
import com.example.aurora.settings.SettingsManager
import com.example.aurora.utils.toDoubleOrZero
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.toString

class WeatherRepositoryImp(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val settingsManager: SettingsManager,

) : WeatherRepository {

    private val apiKey = "97ad72691a7bd2c8f56c772da5029512"
    private val geocodingApiKey = "AIzaSyB9cRwZcC2Kirk3Fy2sCEtPUv3zIqRn6Jk"

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

        // Find duplicate by city name (ID)
        val duplicateForecast = existingForecasts.find { existing ->
            val equalCity = existing.city.name.equals(forecast.city.name, ignoreCase = true)
            val equalLat = abs(existing.city.coord.lat.toDoubleOrZero() - forecast.city.coord.lat.toDoubleOrZero()) < 0.0001
            val equalLon = abs(existing.city.coord.lon.toDoubleOrZero() - forecast.city.coord.lon.toDoubleOrZero()) < 0.0001
            equalCity || (equalLat && equalLon)
        }

        return when {
            // If duplicate exists and is home, preserve home status
            duplicateForecast?.isHome == true -> {
                localDataSource.deleteForecast(duplicateForecast.city.name)
                localDataSource.insertForecast(forecast.copy(isHome = true))
            }
            // If new forecast is marked as home
            forecast.isHome -> {
                // Remove old home flag if exists
                existingForecasts.find { it.isHome }?.let { oldHome ->
                    localDataSource.deleteForecast(oldHome.city.name)
                    localDataSource.insertForecast(oldHome.copy(isHome = false))
                }
                // Delete duplicate if exists and insert new home forecast
                duplicateForecast?.let { localDataSource.deleteForecast(it.city.name) }
                localDataSource.insertForecast(forecast)
            }
            // If duplicate exists (not home)
            duplicateForecast != null -> {
                localDataSource.deleteForecast(duplicateForecast.city.name)
                localDataSource.insertForecast(forecast.copy(isHome = false))
            }
            // New non-duplicate forecast
            else -> localDataSource.insertForecast(forecast.copy(isHome = false))
        }
    }

    override suspend fun deleteForecast(cityName: String): Int {
        try {
            val existingForecasts = localDataSource.getAllForecasts().firstOrNull() ?: emptyList()
            val existingForecast = existingForecasts.find { existing ->
                existing.city.name == cityName
            }

            return if (existingForecast != null) {
                val result = localDataSource.deleteForecast(existingForecast.city.name) // Use existing forecast
                Log.d("WeatherRepositoryImp", "Deleted forecast: $result")
                result
            } else {
                0
            }
        } catch (e: Exception) {
            throw e
        }
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
            settingsManager: SettingsManager
        ): WeatherRepositoryImp {
            return instance ?: synchronized(this) {
                val temp = WeatherRepositoryImp(
                    remoteDataSource,
                    localDataSource,
                    settingsManager
                )
                instance = temp
                temp
            }
        }
    }
}