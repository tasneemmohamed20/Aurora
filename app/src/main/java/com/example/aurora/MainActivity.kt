package com.example.aurora

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aurora.data.remote.RemoteDataSourceImp
import com.example.aurora.data.repo.WeatherRepositoryImp
import com.example.aurora.home.HomeScreen
import com.example.aurora.home.ForecastViewModel
import com.example.aurora.router.Routes
import com.example.aurora.utils.LocationHelper
import com.example.aurora.workers.WeatherWorkManager

class MainActivity : ComponentActivity() {
    private val viewModel: ForecastViewModel by viewModels {
        ForecastViewModel.Factory(
            WeatherRepositoryImp(RemoteDataSourceImp(), this),
            LocationHelper(this),
            WeatherWorkManager(this)
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
//    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.SplashRoute.toString(),
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.HomeRoute.toString()) {
                onScreenChange(true)
                HomeScreen()
            }

            composable(Routes.SplashRoute.toString()) {
                onScreenChange(false)
                SplashScreenUI(
                    onNavigateToHome = {
                        navController.navigate(Routes.HomeRoute.toString()) {
                            popUpTo(Routes.SplashRoute.toString()) { inclusive = true }
                        }
                    }
                )
            }
        }

    }
}
