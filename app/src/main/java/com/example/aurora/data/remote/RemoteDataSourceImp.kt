package com.example.aurora.data.remote

import android.util.Log
import com.example.aurora.data.model.current_weather.CurrentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RemoteDataSourceImp : RemoteDataSource {
    private val weatherApi: ApiServices = RetrofitClient.getRetrofit().create(ApiServices::class.java)

    override suspend fun getCurrentWeather(
        apiKey: String,
        lat: Double,
        lon: Double,
        units: String,
        language: String
    ): Flow<CurrentResponse> = flow {
        val response = weatherApi.getCurrentWeather(apiKey, lat, lon, units, language)
        emit(response)
    }.flowOn(Dispatchers.IO)
        .catch { e ->
            Log.e("RemoteDataSource", "API Error: ${e.message}", e)
            throw e
        }
}