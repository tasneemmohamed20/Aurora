package com.example.aurora.data.repo

import android.content.Context
import com.example.aurora.R
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.remote.RemoteDataSourceImp
import kotlinx.coroutines.flow.Flow

class WeatherRepositoryImp(
    private val remoteDataSource: RemoteDataSourceImp,
    context: Context
) : WeatherRepository {
    private val apiKey = context.getString(R.string.weather_api_key)
    private val geocodingApiKey = context.getString(R.string.MAPS_API_KEY)

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

    override suspend fun getAddressFromGeocoding(
        latlng: String,
        apiKey: String
    ): Flow<String> {
        return remoteDataSource.getAddressFromGeocoding(latlng, geocodingApiKey)

    }
}