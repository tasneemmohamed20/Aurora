package com.example.aurora.data.repo

import android.content.Context
import com.example.aurora.R
import com.example.aurora.data.model.hourly_daily.ForecastResponse
import com.example.aurora.data.remote.RemoteDataSourceImp
import kotlinx.coroutines.flow.Flow

class WeatherRepositoryImp(
    private val remoteDataSource: RemoteDataSourceImp,
    context: Context
) : WeatherRepository {
    private val apiKey = context.getString(R.string.weather_api_key)

//    override suspend fun getWeather(latitude: Double, longitude: Double): Flow<CurrentResponse> =
//        remoteDataSource.getCurrentWeather(
//            apiKey = apiKey,
//            lat = latitude,
//            lon = longitude,
//            language = "en",
//            units = "metric"
//        )
//            .catch { e ->
//                Log.e("WeatherRepo", "Error fetching weather: ${e.message}")
//                throw e
//            }
//            .flowOn(Dispatchers.IO)

    override suspend fun getForecast(
        latitude: Double,
        longitude: Double
    ): Flow<ForecastResponse> {
        return remoteDataSource.getHourlyDailyForecast(
            apiKey = apiKey,
            lat = latitude,
            lon = longitude,
            language = "en",
            units = "metric"
        )
    }
}