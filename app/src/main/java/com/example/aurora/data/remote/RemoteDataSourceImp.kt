package com.example.aurora.data.remote

import android.util.Log
import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RemoteDataSourceImp : RemoteDataSource {
    private val apiServices: ApiServices = RetrofitClient.getRetrofit().create(ApiServices::class.java)
    private val geocodingApi: ApiServices = RetrofitGeoHelper.getRetrofit().create(ApiServices::class.java)

    override suspend fun getHourlyDailyForecast(
        apiKey: String,
        lat: Double,
        lon: Double,
        units: String,
        language: String
    ): Flow<ForecastResponse> {
        val response = apiServices.getHourlyDailyForecast(apiKey, lat, lon, units, language)
        return flow {
            emit(response)
        }.flowOn(Dispatchers.IO)
            .catch { e ->
                Log.e("RemoteDataSource", "API Error: ${e.message}", e)
                throw e
        }
    }

    override suspend fun getAddressFromGeocoding(
        latlng: String,
        apiKey: String
    ): Flow<String> {
        val response = geocodingApi.getAddressFromGeocoding(latlng, apiKey)
        return flow {
            if (response.results.isNotEmpty()) {
                emit(response.results[0].formattedAddress)
            } else {
                emit("No address found")
            }
        }.flowOn(Dispatchers.IO)
            .catch { e ->
                Log.e("RemoteDataSource", "API Error: ${e.message}", e)
                throw e
        }
    }
}