package com.example.aurora.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.aurora.data.model.forecast.City
import com.example.aurora.data.model.forecast.Coord
import com.example.aurora.data.model.forecast.ForecastResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@SmallTest // -> unit test
class DaoTest {
    lateinit var dao : Dao
    lateinit var database: AppDatabase

    val forecast = ForecastResponse(
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

    @Before
     fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()

        dao = database.getForecastDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertForecastTest() = runTest {
        //given
        dao.insertForecast(forecast)

        //when
        val insertedForecast = dao.getForecastByCityName("Test City").first()

        //then
        assertNotNull(insertedForecast)
        assertEquals(insertedForecast.city.name, forecast.city.name)
    }

    @Test
    fun deleteForecastTest() = runTest {
        //given
        dao.insertForecast(forecast)

        //when
        dao.deleteForecast(forecast)

        //then
        val deletedForecast = dao.getForecastByCityName("Test City").first()
        assertEquals(deletedForecast, null)
    }

    @Test
    fun getAllForecastsTest() = runTest {
        //given
        dao.insertForecast(forecast)
        dao.insertForecast(forecast2)

        //when
        val allForecasts = dao.getAllForecasts().first()

        //then
        assertEquals(allForecasts.size, 2)
        assertEquals(allForecasts[0].city.name, forecast.city.name)
        assertEquals(allForecasts[1].city.name, forecast2.city.name)
    }
}
