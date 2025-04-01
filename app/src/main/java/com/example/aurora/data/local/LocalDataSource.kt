package com.example.aurora.data.local

import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun getAllForecasts(): Flow<List<ForecastResponse>>

    suspend fun insertForecast(forecast: ForecastResponse): Long

    suspend fun deleteForecast(forecast: ForecastResponse): Int

    suspend fun getForecastByCityName(cityName: String): Flow<ForecastResponse?>


    // Weather Alert Settings
    suspend fun getAllAlerts(): Flow<List<WeatherAlertSettings>>

    suspend fun insertAlert(alert: WeatherAlertSettings): Long

    suspend fun deleteAlert(alert: WeatherAlertSettings): Int
}