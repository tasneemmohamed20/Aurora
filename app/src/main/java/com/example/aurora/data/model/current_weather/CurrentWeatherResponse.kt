package com.example.aurora.data.model.current_weather

import com.google.gson.annotations.SerializedName

data class CurrentResponse(
	val visibility: Int? = null,
	val timezone: Int? = null,
	val main: Main? = null,
	val clouds: Clouds? = null,
	val sys: Sys? = null,
	val dt: Int? = null,
	val coord: Coord? = null,
	val weather: List<WeatherItem?>? = null,
	val name: String? = null,
	val cod: Int? = null,
	val id: Int? = null,
	val base: String? = null,
	val wind: Wind? = null
)

data class Wind(
	val deg: Int? = null,
	val speed: Any? = null,
	val gust: Any? = null
)

data class Sys(
	val sunrise: Int? = null,
	val sunset: Int? = null
)

data class Coord(
	val lon: Any? = null,
	val lat: Any? = null
)

data class WeatherItem(
	val icon: String? = null,
	val description: String? = null,
	val main: String? = null,
	val id: Int? = null
)

data class Clouds(
	val all: Int? = null
)

data class Main(
	val temp: Any? = null,
	val tempMin: Any? = null,
	val grndLevel: Int? = null,
	val humidity: Int? = null,
	val pressure: Int? = null,
	val seaLevel: Int? = null,
	@SerializedName("feels_like")
	val feelsLike: Double? = null,
	val tempMax: Any? = null
)
