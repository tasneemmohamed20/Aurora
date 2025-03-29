package com.example.aurora.data.local

import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun getAllForecasts(): Flow<List<ForecastResponse>>

    suspend fun insertForecast(forecast: ForecastResponse): Long

    suspend fun deleteForecast(forecast: ForecastResponse): Int
}