package com.example.aurora.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.model.forecast.ForecastTypeConverters

@Database(entities = arrayOf(
    ForecastResponse::class,
    WeatherAlertSettings::class
),
    version = 1)
@TypeConverters(ForecastTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getForecastDao(): Dao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance?: synchronized(this) {
                val INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .build()
                instance = INSTANCE
                INSTANCE
            }
        }
    }
}