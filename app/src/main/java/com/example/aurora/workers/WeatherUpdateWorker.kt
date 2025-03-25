package com.example.aurora.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aurora.data.model.current_weather.CurrentResponse
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
            val location = locationHelper.getLastKnownLocation()
            if (location != null) {
                val repository = WorkerUtils.getRepository()
                val response = repository.getWeather(location.latitude, location.longitude)
                    .firstOrNull()

                if (response != null) {
                    WorkerUtils.cacheWeatherData(response)
                    Result.success()
                } else {
                    Result.retry()
                }
            } else {
                Result.retry()
            }
        } catch (_: SecurityException) {
            Result.retry()
        }
    }
}

object WorkerUtils {
    private var repository: WeatherRepository? = null
    private var cachedWeatherData: CurrentResponse? = null

    fun initRepository(repo: WeatherRepository) {
        repository = repo
    }

    fun getRepository(): WeatherRepository {
        return repository ?: throw IllegalStateException("Repository not initialized")
    }

    fun cacheWeatherData(data: CurrentResponse) {
        cachedWeatherData = data
    }

    fun getCachedWeatherData(): CurrentResponse? = cachedWeatherData
}