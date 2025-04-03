package com.example.aurora.data.repo

import android.content.Context
import com.example.aurora.data.model.forecast.City
import com.example.aurora.data.model.forecast.Coord
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.settings.SettingsManager
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock


class WeatherRepositoryImpTest {

    val forecast1 = ForecastResponse(
        city = City(
            id = 1,
            name = "Test City",
            country = "Test Country",
            population = 1000000,
            timezone = 3600,
            sunrise = 1620000000,
            sunset = 1620050000,
            coord = Coord(
                lat = 0.0,
                lon = 0.0
            )
        ),
        cod = "200",
        message = 0,
        cnt = 7,
        list = emptyList()
    )
    val forecast2 = ForecastResponse(
        city = City(
            id = 2,
            name = "Test City2",
            country = "Test Country",
            population = 1000000,
            timezone = 3600,
            sunrise = 1620000000,
            sunset = 1620050000,
            coord = Coord(
                lat = 1.0,
                lon = 1.0
            )
        ),
        cod = "200",
        message = 0,
        cnt = 7,
        list = emptyList()
    )
    val localForecastList = mutableListOf<ForecastResponse>(forecast1, forecast2)
    val remoteForecastList = mutableListOf<ForecastResponse>(forecast1, forecast2)

    private lateinit var fakeLocalDataSource: FakeLocalDataSource
    private lateinit var fakeRemoteDataSource: FakeLocalDataSource
    private lateinit var repository: WeatherRepositoryImp
    @Before
    fun setUp() {

        fakeLocalDataSource = FakeLocalDataSource(localForecastList)
        fakeRemoteDataSource = FakeLocalDataSource(remoteForecastList)

        repository = WeatherRepositoryImp(
            remoteDataSource = fakeRemoteDataSource,
            localDataSource = fakeLocalDataSource,
            settingsManager = FakeSettingsManager()
        )
    }

    @Test
    fun testGetForecast() = runTest { // uses the remote data source
        val latitude = 0.0
        val longitude = 0.0

        val result = repository.getForecast(latitude, longitude)

        result.collect { forecast ->
            assert(forecast.city.name == "Test City")
            assert(forecast.city.coord.lat == 0.0)
            assert(forecast.city.coord.lon == 0.0)
        }
    }

    @Test
    fun testGetAllForecasts() = runTest { // uses the local data source
        val result = repository.getAllForecasts()

        result.collect { forecasts ->
            assert(forecasts.size == 2)
            assert(forecasts[0].city.name == "Test City")
            assert(forecasts[1].city.name == "Test City2")
        }
    }
}

class FakeSettingsManager : SettingsManager(
    context = mock(Context::class.java)
) {
    private val temperatureUnitValue = UNIT_METRIC
    private val languageValue = LANG_ENGLISH

    override var temperatureUnit: String = temperatureUnitValue

    override var language: String = languageValue

    override fun getDisplayTemperatureUnit(): String = "C"

    override fun getSpeedUnit(): String = "m/s"

    override fun getDisplayUnits(): Pair<String, String> = Pair("C", "m/s")

    companion object {
        const val UNIT_METRIC = "metric"
        const val UNIT_IMPERIAL = "imperial"
        const val UNIT_STANDARD = ""
        const val LANG_ENGLISH = "en"
        const val LANG_ARABIC = "ar"
    }
}