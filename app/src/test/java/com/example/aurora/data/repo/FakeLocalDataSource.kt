package com.example.aurora.data.repo

import com.example.aurora.data.local.LocalDataSource
import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.collections.remove

class FakeLocalDataSource(val forecastList: MutableList<ForecastResponse>?) : LocalDataSource, RemoteDataSource {
    override suspend fun getAllForecasts(): Flow<List<ForecastResponse>> = flow {
        emit(forecastList ?:emptyList())
    }

    override suspend fun insertForecast(forecast: ForecastResponse): Long {
        forecastList?.add(forecast)
        return if (forecastList?.contains(forecast) == true) 1 else 0
    }


    override suspend fun deleteForecast(city: String): Int {
        val forecast = forecastList?.find { it.city.name == city }
        return if (forecast != null && forecastList.remove(forecast)) 1 else 0
    }

    override suspend fun getForecastByCityName(cityName: String): Flow<ForecastResponse?> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllAlerts(): Flow<List<WeatherAlertSettings>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertAlert(alert: WeatherAlertSettings): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlert(alert: WeatherAlertSettings): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getHourlyDailyForecast(
        apiKey: String,
        lat: Double,
        lon: Double,
        units: String,
        language: String
    ): Flow<ForecastResponse> = flow {
        val matchingForecast = forecastList?.find { forecast ->
            val forecastLat = forecast.city.coord.lat.toString().toDoubleOrNull()
            val forecastLon = forecast.city.coord.lon.toString().toDoubleOrNull()

            forecastLat != null && forecastLon != null &&
                    forecastLat == lat && forecastLon == lon
        }

        matchingForecast?.let {
            emit(it)
        } ?: throw Exception("No forecast found for coordinates: $lat, $lon")
    }

    override suspend fun getAddressFromGeocoding(
        latlng: String,
        apiKey: String
    ): Flow<String> {
        TODO("Not yet implemented")
    }
}