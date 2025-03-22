package com.example.aurora.data.repo

import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.remote.RemoteDataSourceImp
import kotlinx.coroutines.flow.Flow

class WeatherRepositoryImp(
    private val remoteDataSource: RemoteDataSourceImp
) : WeatherRepository {
    private val apiKey : String = "97ad72691a7bd2c8f56c772da5029512"
    override suspend fun getWeather() : Flow<CurrentResponse> =
        remoteDataSource.getCurrentWeather(
            apiKey = apiKey,
            lat = 30.06263,
            lon = 31.24967,
            language = "en",
            units = "metric"
        )
    }
