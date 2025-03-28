package com.example.aurora.data.remote

import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.model.map.GeocodingResponse
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

    @GET("maps/api/geocode/json")
    suspend fun getAddressFromGeocoding(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String
    ): GeocodingResponse
}