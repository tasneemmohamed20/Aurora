package com.example.aurora.data.repo

import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    // Remote data source
    suspend fun getForecast(latitude: Double, longitude: Double): Flow<ForecastResponse>
    suspend fun getAddressFromGeocoding(latlng: String, apiKey: String): Flow<String>

    // Local data source
    suspend fun getAllForecasts(): Flow<List<ForecastResponse>>
    suspend fun insertForecast(forecast: ForecastResponse): Long
    suspend fun deleteForecast(forecast: ForecastResponse): Int
}