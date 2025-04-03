package com.example.aurora.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.aurora.data.model.WeatherAlertSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@MediumTest // -> integration test
class LocalDataSourceImpTest {
    private lateinit var database: AppDatabase
    private lateinit var dao : Dao
    private lateinit var localDataSource: LocalDataSourceImp

    val alert = WeatherAlertSettings(
        id = "1",
        duration = 2,
        useDefaultSound = true,
        startTime = 1
    )
    val alert2 = WeatherAlertSettings(
        id = "2",
        duration = 3,
        useDefaultSound = false,
        startTime = 2
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.getForecastDao()
        localDataSource = LocalDataSourceImp(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllAlertsTest() = runTest {

        localDataSource.insertAlert(alert)
        localDataSource.insertAlert(alert2)

        // when
        val allAlerts = localDataSource.getAllAlerts().first()

        // then
        assert(allAlerts.size == 2)
        assert(allAlerts.any { it.id == "1" })
        assert(allAlerts.any { it.id == "2" })
    }

    @Test
    fun insertAlertTest() = runTest {
        // when
        val insertResult = localDataSource.insertAlert(alert)
        val insertedAlert = localDataSource.getAllAlerts().first().firstOrNull()

        // then
        assert(insertResult > 0)
        assert(insertedAlert != null)
        assert(insertedAlert?.id == alert.id)
        assert(insertedAlert?.duration == alert.duration)
        assert(insertedAlert?.useDefaultSound == alert.useDefaultSound)
        assert(insertedAlert?.startTime == alert.startTime)
    }

    @Test
    fun deleteAlertTest() = runTest {
        // given
        localDataSource.insertAlert(alert)

        // when
        val deleteResult = localDataSource.deleteAlert(alert)
        val remainingAlerts = localDataSource.getAllAlerts().first()

        // then
        assert(deleteResult == 1)
        assert(remainingAlerts.isEmpty())
    }


}