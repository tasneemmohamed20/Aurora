package com.example.aurora.data.model.forecast

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(
	tableName = "forecast_table",
	primaryKeys = ["city_coord_lon", "city_coord_lat"]
)
data class ForecastResponse(
	@Embedded(prefix = "city_") @NonNull
	val city: City,
	val cnt: Int? = null,
	val cod: String? = null,
	val message: Int? = null,
	val list: List<ListItem?>? = null,
	var isHome: Boolean = false
)

data class ListItem(
	val dt: Int? = null,
	val pop: Any? = null,
	val visibility: Int? = null,
	val dtTxt: String? = null,
	val weather: List<WeatherItem?>? = null,
	val main: Main? = null,
	val clouds: Clouds? = null,
	val sys: Sys? = null,
	val wind: Wind? = null,
	val rain: Rain? = null
)

data class Rain(
	val jsonMember3h: Any? = null
)

data class City(
	val country: String? = null,
	@Embedded(prefix = "coord_") @NonNull
	val coord: Coord,
	val sunrise: Int? = null,
	val timezone: Int? = null,
	val sunset: Int? = null,
	@NonNull @ColumnInfo(name = "city_name")
	val name: String,
	val id: Int? = null,
	val population: Int? = null
)

data class Coord(
	@NonNull @ColumnInfo(name = "lon")
	val lon: Any,
	@NonNull @ColumnInfo(name = "lat")
	val lat: Any
)

data class Clouds(
	val all: Int? = null
)

data class Main(
	val temp: Any? = null,
	@SerializedName("temp_min")
	val tempMin: Double? = null,
	val grndLevel: Int? = null,
	val tempKf: Int? = null,
	val humidity: Int? = null,
	val pressure: Int? = null,
	val seaLevel: Int? = null,
	@SerializedName("feels_like")
	val feelsLike: Any? = null,
	@SerializedName("temp_max")
	val tempMax: Double? = null
)

data class Wind(
	val deg: Int? = null,
	val speed: Any? = null,
	val gust: Any? = null
)

data class WeatherItem(
	val icon: String? = null,
	val description: String? = null,
	val main: String? = null,
	val id: Int? = null
)

data class Sys(
	val pod: String? = null
)

