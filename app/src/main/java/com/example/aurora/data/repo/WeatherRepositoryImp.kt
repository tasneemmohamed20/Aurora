// File: `app/src/main/java/com/example/aurora/data/repo/WeatherRepositoryImp.kt`
package com.example.aurora.data.repo

import android.content.Context
import android.util.Log
import com.example.aurora.R
import com.example.aurora.data.local.LocalDataSource
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow

class WeatherRepositoryImp private constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val context: Context
) : WeatherRepository {

    private val apiKey = context.getString(R.string.weather_api_key)
    private val geocodingApiKey = context.getString(R.string.MAPS_API_KEY)

    private val currentLocale: String
        get() = context.resources.configuration.locales[0].toString().substring(0, 2)

    override suspend fun getForecast(
        latitude: Double,
        longitude: Double
    ): Flow<ForecastResponse> {
        Log.d("WeatherRepositoryImp", "Locale: $currentLocale")
        return remoteDataSource.getHourlyDailyForecast(
            apiKey = apiKey,
            lat = latitude,
            lon = longitude,
            language = currentLocale,
            units = "metric"
        )
    }

    override suspend fun getAddressFromGeocoding(
        latlng: String,
        apiKey: String
    ): Flow<String> {
        return remoteDataSource.getAddressFromGeocoding(latlng, geocodingApiKey)
    }

    override suspend fun getAllForecasts(): Flow<List<ForecastResponse>> {
        return localDataSource.getAllForecasts()
    }

    override suspend fun insertForecast(forecast: ForecastResponse): Long {
        return localDataSource.insertForecast(forecast)
    }

    override suspend fun deleteForecast(forecast: ForecastResponse): Int {
        return localDataSource.deleteForecast(forecast)
    }

    companion object {
        private var instance: WeatherRepositoryImp? = null
        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource,
            context: Context
        ): WeatherRepositoryImp {
            return instance ?: synchronized(this) {
                val temp = WeatherRepositoryImp(
                    remoteDataSource,
                    localDataSource,
                    context
                )
                instance = temp
                temp
            }
        }
    }
}