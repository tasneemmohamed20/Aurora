package com.example.aurora.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.Flow


@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: ForecastResponse): Long

    @Delete
    suspend fun deleteForecast(forecast: ForecastResponse): Int

    @Query("SELECT * FROM forecast_table")
    fun getAllForecasts(): Flow<List<ForecastResponse>>
}