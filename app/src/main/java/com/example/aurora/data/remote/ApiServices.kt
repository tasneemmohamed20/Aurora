package com.example.aurora.data.remote

import com.example.aurora.data.model.forecast.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {

    @GET("forecast")
    suspend fun getHourlyDailyForecast(
        @Query("appid") apiKey: String,
        @Query("lat") lot: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "en"
    ): ForecastResponse
}