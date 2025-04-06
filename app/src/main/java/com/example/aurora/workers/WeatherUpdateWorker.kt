package com.example.aurora.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.repo.WeatherRepository
import com.example.aurora.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class WeatherUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val locationHelper = LocationHelper(context)


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val location = locationHelper.getCurrentLocation()
                ?: locationHelper.getLastLocation()

            if (location == null) {
                Log.d("WeatherUpdateWorker", "No location available")
                return@withContext Result.retry()
            }

            val repository = WorkerUtils.getRepository()
            val response = repository.getForecast(location.latitude, location.longitude)
                .firstOrNull()

            if (response != null) {
                WorkerUtils.cacheWeatherData(response)
                Log.d("WeatherUpdateWorker", "Weather update successful")
                Result.success()
            } else {
                Log.d("WeatherUpdateWorker", "No weather data received")
                Result.retry()
            }
        } catch (e: SecurityException) {
            Log.e("WeatherUpdateWorker", "Security exception", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e("WeatherUpdateWorker", "Error updating weather", e)
            Result.failure()
        }
    }
}

object WorkerUtils {
    private var repository: WeatherRepository? = null
    private var cachedWeatherData: ForecastResponse? = null

    fun initRepository(repo: WeatherRepository) {
        repository = repo
    }

    fun getRepository(): WeatherRepository {
        return repository ?: throw IllegalStateException("Repository not initialized")
    }

    fun cacheWeatherData(data: ForecastResponse) {
        cachedWeatherData = data
    }

    fun getCachedWeatherData(): ForecastResponse? = cachedWeatherData
}