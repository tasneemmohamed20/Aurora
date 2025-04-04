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

        // More detailed logging for debugging
        forecast.city.coord?.let { newCoord ->
            Log.d("WeatherRepositoryImp", "New coordinates: lat=${newCoord.lat}, lon=${newCoord.lon}")
            existingForecasts.forEach { existing ->
                existing.city.coord?.let { existingCoord ->
                    Log.d("WeatherRepositoryImp", "Existing coordinates for ${existing.city.name}: lat=${existingCoord.lat}, lon=${existingCoord.lon}")
                }
            }
        }

        // Find location with same coordinates - with more precise comparison
        val duplicateByCoords = existingForecasts.find { existing ->
            existing.city.coord?.let { existingCoord ->
                forecast.city.coord?.let { newCoord ->
                    // Using equals with some tolerance for floating point comparison
                    val latEqual = abs(existingCoord.lat.toDoubleOrZero() - newCoord.lat.toDoubleOrZero()) < 0.000001
                    val lonEqual = abs(existingCoord.lon.toDoubleOrZero() - newCoord.lon.toDoubleOrZero()) < 0.000001
                    latEqual && lonEqual
                }
            } == true
        }

        Log.d("WeatherRepositoryImp", "Found duplicate: $duplicateByCoords")


        return when {
            forecast.isHome -> {
                existingForecasts.find { it.isHome }?.let { oldHome ->
                    val result = localDataSource.deleteForecast(oldHome.city.name)
                    Log.d("WeatherRepositoryImp", "Deleted old home forecast: $result")
                    localDataSource.insertForecast(oldHome.copy(isHome = false))
                }
//                duplicateByCoords?.let { localDataSource.deleteForecast(it.city.name) }
                localDataSource.insertForecast(forecast)
            }
            duplicateByCoords != null -> {
                val isHome = duplicateByCoords.isHome
//                val result =localDataSource.deleteForecast(duplicateByCoords.city.name)
//                Log.d("WeatherRepositoryImp", "Deleted old home forecast: $result")
                localDataSource.insertForecast(forecast.copy(isHome = isHome))
            }
            else -> {

                    localDataSource.insertForecast(forecast.copy(isHome = false))

            }
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