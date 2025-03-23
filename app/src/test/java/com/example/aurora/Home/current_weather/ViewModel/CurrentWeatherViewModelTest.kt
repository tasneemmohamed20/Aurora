package com.example.aurora.Home.current_weather.ViewModel

import android.content.Context
import com.example.aurora.data.model.current_weather.CurrentResponse
import com.example.aurora.data.model.current_weather.Main
import com.example.aurora.data.model.current_weather.WeatherItem
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentWeatherViewModelTest {
 @Mock
 private lateinit var repository: WeatherRepository

 @Mock
 private lateinit var context: Context

 private lateinit var viewModel: CurrentWeatherViewModel
 private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

 @Before
 fun setup() {
  MockitoAnnotations.openMocks(this)
  Dispatchers.setMain(testDispatcher)
  viewModel = CurrentWeatherViewModel(repository, context)
 }

 @After
 fun tearDown() {
  Dispatchers.resetMain()
 }

 @Test
 fun `fetchWeatherData should update state with success when API call succeeds`() = runTest {
  // Given
  val mockWeatherResponse = CurrentResponse(
   cod = 200,
   main = Main(temp = 20.0),
   weather = listOf(WeatherItem(description = "Clear sky"))
  )

  `when`(repository.getWeather(0.0, 0.0)).thenReturn(
   flowOf(mockWeatherResponse)
  )

  // When
  viewModel.setupLocationUpdates()

  // Then
  val currentState = viewModel.weatherState.value
  assert(currentState is UiState.Success)
  if (currentState is UiState.Success) {
   assertEquals(mockWeatherResponse, currentState.data)
  }
 }

 @Test
 fun `fetchWeatherData should update state with error when API call fails`(): Unit = runTest {
  // Given
  val errorResponse = CurrentResponse(cod = 404)

  `when`(repository.getWeather(0.0, 0.0)).thenReturn(
   flowOf(errorResponse)
  )

  // When
  viewModel.setupLocationUpdates()

  // Then
  val currentState = viewModel.weatherState.value
  assert(currentState is UiState.Error)
  if (currentState is UiState.Error) {
   assertEquals("API Error: 404", currentState.message)
  }
 }

 @Test
 fun `initial state should be Loading`() {
  // Then
  assert(viewModel.weatherState.value is UiState.Loading)
 }

 @Test
 fun `cityName should update when location changes`(): Unit = runTest {
  // Given
  val expectedCity = "Test City"
  viewModel.setupLocationUpdates()

  // When
  // Simulate location update (you might need to modify LocationHelper for testing)

  // Then
  assertEquals(expectedCity, viewModel.cityName.value)
 }
}