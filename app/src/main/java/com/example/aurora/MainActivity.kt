package com.example.aurora

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aurora.Home.current_weather.ViewModel.CurrentWeatherViewModel
import com.example.aurora.data.remote.RemoteDataSourceImp
import com.example.aurora.data.repo.WeatherRepositoryImp
import com.example.aurora.router.Routes

class MainActivity : ComponentActivity() {
    private val viewModel: CurrentWeatherViewModel by viewModels {
        CurrentWeatherViewModel.WeatherViewModelFactory(
            WeatherRepositoryImp(RemoteDataSourceImp(), this),
            this
        )
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                viewModel.setupLocationUpdates()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewModel.setupLocationUpdates()
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppRoutes { isSplashScreen ->
                if (!isSplashScreen) {
                    enableEdgeToEdge()
                }
            }
        }
        requestLocationPermission()
    }
}

@Composable
fun AppRoutes(onScreenChange: (Boolean) -> Unit = {}) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.SplashRoute,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<Routes.HomeRoute> {
            onScreenChange(true)
            HomeScreen()
        }

        composable<Routes.SplashRoute> {
            onScreenChange(false)
            SplashScreenUI(
                onNavigateToHome = {
                    navController.navigate(Routes.HomeRoute) {
                        popUpTo(Routes.SplashRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}