package com.example.aurora.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow


@Dao
interface Dao {

    // Forecast
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: ForecastResponse): Long

    @Query("DELETE FROM forecast_table WHERE city_city_name = :cityName")
    suspend fun deleteForecast(cityName: String): Int

    @Query("SELECT * FROM forecast_table")
    fun getAllForecasts(): Flow<List<ForecastResponse>>

    @Query("SELECT * FROM forecast_table WHERE city_city_name = :cityName")
    fun getForecastByCityName(cityName: String): Flow<ForecastResponse?>

    // Weather Alert Settings

    @Query("SELECT * FROM weather_alerts")
    fun getAllWeatherAlerts(): Flow<List<WeatherAlertSettings>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeatherAlert(weatherAlert: WeatherAlertSettings): Long

    @Delete
    suspend fun deleteWeatherAlert(weatherAlert: WeatherAlertSettings) : Int
}