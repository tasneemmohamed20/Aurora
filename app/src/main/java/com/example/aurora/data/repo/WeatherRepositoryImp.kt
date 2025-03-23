package com.example.aurora.data.repo

import android.content.Context
import android.util.Log
import com.example.aurora.R
import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.remote.RemoteDataSourceImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

class WeatherRepositoryImp(
    private val remoteDataSource: RemoteDataSourceImp,
    private val context: Context
) : WeatherRepository {
    private val apiKey = context.getString(R.string.weather_api_key)

    override suspend fun getWeather(latitude: Double, longitude: Double): Flow<CurrentResponse> =
        remoteDataSource.getCurrentWeather(
            apiKey = apiKey,
            lat = latitude,
            lon = longitude,
            language = "en",
            units = "metric"
        )
            .catch { e ->
                Log.e("WeatherRepo", "Error fetching weather: ${e.message}")
                throw e
            }
            .flowOn(Dispatchers.IO)

    override suspend fun getLastKnownWeather(): Flow<CurrentResponse> =
        remoteDataSource.getCurrentWeather(
            apiKey = apiKey,
            lat = 0.0,
            lon = 0.0,
            language = "en",
            units = "metric"
        )
            .flowOn(Dispatchers.IO)
}