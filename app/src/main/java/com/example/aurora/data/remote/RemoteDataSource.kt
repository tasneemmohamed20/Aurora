package com.example.aurora.data.remote

import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
//    suspend fun getCurrentWeather(
//        apiKey: String,
//        lat: Double,
//        lon: Double,
//        units: String = "metric",
//        language: String = "en"
//    ): Flow<CurrentResponse>

    suspend fun getHourlyDailyForecast(
        apiKey: String,
        lat: Double,
        lon: Double,
        units: String = "metric",
        language: String = "en"
    ): Flow<ForecastResponse>
}