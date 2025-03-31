// File: `app/src/main/java/com/example/aurora/data/repo/WeatherRepositoryImp.kt`
package com.example.aurora.data.repo

import android.content.Context
import android.util.Log
import com.example.aurora.R
import com.example.aurora.data.local.LocalDataSource
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class WeatherRepositoryImp private constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val context: Context
) : WeatherRepository {

    private val apiKey = context.getString(R.string.weather_api_key)
    private val geocodingApiKey = context.getString(R.string.MAPS_API_KEY)

    private val currentLocale: String
        get() = context.resources.configuration.locales[0].toString().substring(0, 2)

    override suspend fun getForecast(
        latitude: Double,
        longitude: Double
    ): Flow<ForecastResponse> {
        Log.d("WeatherRepositoryImp", "Locale: $currentLocale")
        return remoteDataSource.getHourlyDailyForecast(
            apiKey = apiKey,
            lat = latitude,
            lon = longitude,
            language = currentLocale,
            units = "metric"
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

    override suspend fun getAllForecasts(): Flow<List<ForecastResponse>> {
        return localDataSource.getAllForecasts()
    }

    override suspend fun insertForecast(forecast: ForecastResponse): Long {
        val existingForecasts = localDataSource.getAllForecasts().firstOrNull() ?: emptyList()

        // Create a copy of the forecast with the correct isHome status
        val forecastToInsert = if (forecast.isHome) {
            // If this is meant to be home, ensure isHome is true
            forecast.copy(isHome = true)
        } else {
            // If not meant to be home, preserve existing home status if it exists
            val existingForecast = existingForecasts.find { it.city.name == forecast.city.name }
            forecast.copy(isHome = existingForecast?.isHome == true)
        }

        // If setting a new home, unmark any other home locations
        if (forecastToInsert.isHome) {
            existingForecasts
                .filter { it.isHome && it.city.name != forecastToInsert.city.name }
                .forEach { oldHomeForecast ->
                    localDataSource.deleteForecast(oldHomeForecast)
                    localDataSource.insertForecast(oldHomeForecast.copy(isHome = false))
                }
        }

        // Delete existing forecast if it exists
        existingForecasts
            .find { it.city.name == forecastToInsert.city.name }
            ?.let { localDataSource.deleteForecast(it) }

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

    companion object {
        private var instance: WeatherRepositoryImp? = null
        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource,
            context: Context
        ): WeatherRepositoryImp {
            return instance ?: synchronized(this) {
                val temp = WeatherRepositoryImp(
                    remoteDataSource,
                    localDataSource,
                    context
                )
                instance = temp
                temp
            }
        }
    }
}