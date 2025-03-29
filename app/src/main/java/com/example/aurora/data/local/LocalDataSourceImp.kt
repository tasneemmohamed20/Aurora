package com.example.aurora.data.local

import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImp (private val dao: Dao) : LocalDataSource {

    override suspend fun getAllForecasts(): Flow<List<ForecastResponse>> {
        return dao.getAllForecasts()
    }

    override suspend fun insertForecast(forecast: ForecastResponse): Long {
        return dao.insertForecast(forecast)
    }

    override suspend fun deleteForecast(forecast: ForecastResponse): Int {
        return dao.deleteForecast(forecast)
    }

}