package com.example.aurora.data.repo

import com.example.aurora.data.model.current_weather.CurrentResponse
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeather(): Flow<CurrentResponse>
}