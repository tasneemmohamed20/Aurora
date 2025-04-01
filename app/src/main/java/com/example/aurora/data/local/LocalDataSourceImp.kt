package com.example.aurora.data.local

import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImp (private val dao: Dao) : LocalDataSource {

    // Forecast

    override suspend fun getAllForecasts(): Flow<List<ForecastResponse>> {
        return dao.getAllForecasts()
    }

    override suspend fun insertForecast(forecast: ForecastResponse): Long {
        return dao.insertForecast(forecast)
    }

    override suspend fun deleteForecast(forecast: ForecastResponse): Int {
        return dao.deleteForecast(forecast)
    }

    override suspend fun getForecastByCityName(cityName: String): Flow<ForecastResponse?> {
        return dao.getForecastByCityName(cityName)
    }

    // Weather Alert Settings

    override suspend fun getAllAlerts(): Flow<List<WeatherAlertSettings>> {
        return dao.getAllWeatherAlerts()
    }

    override suspend fun insertAlert(alert: WeatherAlertSettings): Long {
        return dao.insertWeatherAlert(alert)
    }

    override suspend fun deleteAlert(alert: WeatherAlertSettings): Int {
        return dao.deleteWeatherAlert(alert)
    }

}