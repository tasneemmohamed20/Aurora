package com.example.aurora.data.remote

import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.model.hourly_daily.HourlyDailyResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("appid") apiKey: String,
        @Query("lat") lot: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "en"
    ): CurrentResponse

    @GET("forecast")
    suspend fun getHourlyDailyForecast(
        @Query("appid") apiKey: String,
        @Query("lat") lot: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "en"
    ): HourlyDailyResponse
}