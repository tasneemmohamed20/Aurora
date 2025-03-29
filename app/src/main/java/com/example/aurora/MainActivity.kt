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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aurora.data.local.AppDatabase
import com.example.aurora.data.local.LocalDataSourceImp
import com.example.aurora.data.model.map.Location
import com.example.aurora.data.remote.RemoteDataSourceImp
import com.example.aurora.data.repo.WeatherRepositoryImp
import com.example.aurora.favorites.FavViewModel
import com.example.aurora.favorites.FavoriteScreen
import com.example.aurora.home.ForecastViewModel
import com.example.aurora.home.HomeScreen
import com.example.aurora.map.MapScreen
import com.example.aurora.map.MapsViewModel
import com.example.aurora.router.Routes
import com.example.aurora.utils.LocationHelper
import com.example.aurora.workers.WeatherWorkManager
import com.google.android.libraries.places.api.Places
import com.google.maps.android.ktx.BuildConfig
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val viewModel: ForecastViewModel by viewModels {
        ForecastViewModel.Factory(
            WeatherRepositoryImp.getInstance(
                RemoteDataSourceImp(),
                LocalDataSourceImp(
                    AppDatabase.getInstance(this).getForecastDao()
                ),
                this
            ),
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

    override fun onResume() {
        super.onResume()
        enableEdgeToEdge()
    }


}

@Composable
fun AppRoutes(onScreenChange: (Boolean) -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val placesClient = remember {
        try {
            Places.createClient(context)
        } catch (e: IllegalStateException) {
            Places.initialize(context,
                context.resources.getString(R.string.MAPS_API_KEY),
                )
            Places.createClient(context)
        }
    }
    val forecastViewModel: ForecastViewModel = viewModel(
        factory = ForecastViewModel.Factory(
            WeatherRepositoryImp.getInstance(
                RemoteDataSourceImp(),
                LocalDataSourceImp(
                    AppDatabase.getInstance(context).getForecastDao()
                ),
                context
            ),
            LocationHelper(context),
            WeatherWorkManager(context)
        )
    )

    val mapsViewModel : MapsViewModel = viewModel(
        factory = MapsViewModel.Factory(
            LocationHelper(context),
            WeatherRepositoryImp.getInstance(
                RemoteDataSourceImp(),
                LocalDataSourceImp(
                    AppDatabase.getInstance(context).getForecastDao()
                ),
                context
            ),
            placesClient
        )
    )

    val favViewModel: FavViewModel = viewModel(
        factory = FavViewModel.Factory(
            WeatherRepositoryImp.getInstance(
                RemoteDataSourceImp(),
                LocalDataSourceImp(
                    AppDatabase.getInstance(context).getForecastDao()
                ),
                context
            )
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.SplashRoute.toString(),
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.HomeRoute.toString()) {
                LaunchedEffect(Unit) { onScreenChange(true) }
                HomeScreen(
                    forecastViewModel = forecastViewModel,
                    onNavigateToFav = {
                        navController.navigate(Routes.FavoritesRoute.toString())
                    }
                )
            }

            composable(Routes.SplashRoute.toString()) {
                LaunchedEffect(Unit) { onScreenChange(false) }
                SplashScreenUI(
                    onNavigateToHome = {
                        navController.navigate(Routes.HomeRoute.toString()) {
                            popUpTo(Routes.SplashRoute.toString()) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "map/{lat}/{lon}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.FloatType },
                    navArgument("lon") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 30.0444
                val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 31.2357

                MapScreen(
                    location = Location(lat, lon),
                    onLocationSelected = { location ->
                        forecastViewModel.updateLocation(location)
                        navController.popBackStack()
                    },
                    viewModel = mapsViewModel
                )
            }

            composable(Routes.FavoritesRoute.toString()) {
                FavoriteScreen(
                    viewModel = favViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSearchClick = {
                        val currentLocation = mapsViewModel.location.value
                        val lat = currentLocation?.lat ?: 30.0444
                        val lon = currentLocation?.lng ?: 31.2357
                        navController.navigate(Routes.MapRoute(lat, lon).toString())
                    }
                )
            }
        }

    }
}
