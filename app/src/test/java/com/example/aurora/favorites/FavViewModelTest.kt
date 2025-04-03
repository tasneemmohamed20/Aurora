package com.example.aurora.favorites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.repo.WeatherRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavViewModelTest {

    lateinit var favViewModel: FavViewModel
    lateinit var repository: WeatherRepository
    private val testDispatcher = StandardTestDispatcher()


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setupViewModel() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        repository = mockk(relaxed = true)
        favViewModel = FavViewModel(repository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadFavorites() = runTest {
        // given
        val testForecasts = listOf(ForecastResponse(mockk(relaxed = true)))
        coEvery { repository.getAllForecasts() } returns flowOf(testForecasts)

        // when
        favViewModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // then
        assert(favViewModel.uiState.value is FavUiState.Success)
        assert((favViewModel.uiState.value as FavUiState.Success).forecasts == testForecasts)
    }

    @Test
    fun testDeleteFavorite() = runTest {
        // given
        val forecast = ForecastResponse(mockk(relaxed = true))
        val testForecasts = listOf(forecast)
        coEvery { repository.deleteForecast(forecast) } returns 1
        coEvery { repository.getAllForecasts() } returns flowOf(testForecasts)

        // Load initial state
        favViewModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        // when
        favViewModel.deleteFavorite(forecast)
        testDispatcher.scheduler.advanceUntilIdle()

        // then
        assert(favViewModel.uiState.value is FavUiState.Success)
    }

}