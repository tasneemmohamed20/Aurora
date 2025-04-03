package com.example.aurora.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aurora.data.model.forecast.ForecastResponse
import com.example.aurora.data.repo.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class FavViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<FavUiState>(FavUiState.Loading)
    val uiState: StateFlow<FavUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                repository.getAllForecasts()
                    .distinctUntilChanged()
                    .collect { forecasts ->
                    _uiState.value = FavUiState.Success(forecasts)
                }
            } catch (e: Exception) {
                _uiState.value = FavUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteFavorite(forecast: ForecastResponse) {
        viewModelScope.launch {
            // Prevent deletion if the forecast is marked as home.
            if (forecast.isHome) {
                // Optionally you can update UI state here with an error message.
                return@launch
            }
            repository.deleteForecast(forecast)
        }
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavViewModel::class.java)) {
                return FavViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class FavUiState {
    object Loading : FavUiState()
    data class Success(val forecasts: List<ForecastResponse>) : FavUiState()
    data class Error(val message: String) : FavUiState()
}