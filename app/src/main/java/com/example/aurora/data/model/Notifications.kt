package com.example.aurora.data.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "weather_alerts")
data class WeatherAlertSettings(
    @PrimaryKey @NonNull
    val id: String = UUID.randomUUID().toString(),
    val duration: Long,
    val useDefaultSound: Boolean,
    val startTime: Long = System.currentTimeMillis()
)