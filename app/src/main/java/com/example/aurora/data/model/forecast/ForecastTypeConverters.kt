package com.example.aurora.data.model.forecast

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ForecastTypeConverters {
    @TypeConverter
    fun fromListItemList(value: List<ListItem?>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toListItemList(value: String): List<ListItem?>? {
        val listType = object : TypeToken<List<ListItem?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromWeatherItemList(value: List<WeatherItem?>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toWeatherItemList(value: String): List<WeatherItem?>? {
        val listType = object : TypeToken<List<WeatherItem?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    // For Any type
    @TypeConverter
    fun fromAny(value: Any?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toAny(value: String): Any? {
        return Gson().fromJson(value, Any::class.java)
    }

    // For Clouds
    @TypeConverter
    fun fromClouds(value: Clouds?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toClouds(value: String): Clouds? {
        return Gson().fromJson(value, Clouds::class.java)
    }

    // For Main
    @TypeConverter
    fun fromMain(value: Main?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMain(value: String): Main? {
        return Gson().fromJson(value, Main::class.java)
    }

    // For Sys
    @TypeConverter
    fun fromSys(value: Sys?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSys(value: String): Sys? {
        return Gson().fromJson(value, Sys::class.java)
    }

    // For Wind
    @TypeConverter
    fun fromWind(value: Wind?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toWind(value: String): Wind? {
        return Gson().fromJson(value, Wind::class.java)
    }

    // For Rain
    @TypeConverter
    fun fromRain(value: Rain?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toRain(value: String): Rain? {
        return Gson().fromJson(value, Rain::class.java)
    }

    // For Coord
    @TypeConverter
    fun fromCoord(value: Coord?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCoord(value: String): Coord? {
        return Gson().fromJson(value, Coord::class.java)
    }
}