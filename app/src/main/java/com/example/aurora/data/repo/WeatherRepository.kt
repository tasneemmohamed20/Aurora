package com.example.aurora.data.repo

import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getForecast(latitude: Double, longitude: Double): Flow<ForecastResponse>
    suspend fun getAddressFromGeocoding(latlng: String, apiKey: String): Flow<String>
}