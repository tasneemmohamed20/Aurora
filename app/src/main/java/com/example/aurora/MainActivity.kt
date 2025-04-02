package com.example.aurora

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.aurora.notifications.NotificationsScreen
import com.example.aurora.notifications.NotificationsViewModel
import com.example.aurora.router.Routes
import com.example.aurora.settings.SettingsManager
import com.example.aurora.settings.SettingsScreen
import com.example.aurora.settings.SettingsViewModel
import com.example.aurora.utils.LocaleHelper
import com.example.aurora.utils.LocationHelper
import com.example.aurora.workers.WeatherWorkManager
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private lateinit var settingsManager: SettingsManager

    override fun attachBaseContext(newBase: Context) {
        settingsManager = SettingsManager(newBase)
        val language = settingsManager.language
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language))
        // Force RTL on initial creation
        window?.decorView?.layoutDirection =
            if (language == "ar") View.LAYOUT_DIRECTION_RTL
            else View.LAYOUT_DIRECTION_LTR
    }

    private val viewModel: ForecastViewModel by viewModels {
        ForecastViewModel.Factory(
            WeatherRepositoryImp.getInstance(
                RemoteDataSourceImp(),
                LocalDataSourceImp(
                    AppDatabase.getInstance(this).getForecastDao()
                ),
                this,
                settingsManager
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

    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome = _navigateToHome.asStateFlow()


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "WEATHER_ALERT" -> {
                _navigateToHome.value = true
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
        // Force RTL/LTR at activity creation
        val language = settingsManager.language
        window.decorView.layoutDirection =
            if (language == "ar") View.LAYOUT_DIRECTION_RTL
            else View.LAYOUT_DIRECTION_LTR

        setContent {
            // Use CompositionLocalProvider to force layout direction
            CompositionLocalProvider(
                LocalLayoutDirection provides if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppRoutes { isSplashScreen ->
                        if (!isSplashScreen) {
                            enableEdgeToEdge()
                        }
                    }
                }
            }
        }
        requestLocationPermission()
        handleIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        val language = settingsManager.language
        val context = LocaleHelper.setLocale(this, language)
        resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)

        // Force RTL/LTR on configuration change
        window.decorView.layoutDirection =
            if (language == "ar") View.LAYOUT_DIRECTION_RTL
            else View.LAYOUT_DIRECTION_LTR

        // Recreate activity to ensure all composables get the new direction
//        recreate()
    }

    override fun onResume() {
        super.onResume()
        enableEdgeToEdge()
    }

}

@Composable
fun AppRoutes(onScreenChange: (Boolean) -> Unit = {}) {
    val navController = rememberNavController()
    val view = LocalView.current
    val activity = remember(view) { view.context as MainActivity }
    val context = LocalContext.current
    val settingsManager = SettingsManager(context)
    val placesClient = remember {
        try {
            Places.createClient(context)
        } catch (_: IllegalStateException) {
            Places.initialize(context,
                context.resources.getString(R.string.MAPS_API_KEY),
                )
            Places.createClient(context)
        }
    }


    LaunchedEffect(activity.navigateToHome) {
        activity.navigateToHome.collect { shouldNavigate ->
            if (shouldNavigate) {
                navController.navigate(Routes.HomeRoute.toString()) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }


    val forecastViewModel: ForecastViewModel = viewModel(
        factory = ForecastViewModel.Factory(
            WeatherRepositoryImp.getInstance(
                RemoteDataSourceImp(),
                LocalDataSourceImp(
                    AppDatabase.getInstance(context).getForecastDao()
                ),
                context,
                settingsManager
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
                context,
                settingsManager
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
                context,
                settingsManager
            )
        )
    )

    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.Factory(
            repository = WeatherRepositoryImp.getInstance(
                RemoteDataSourceImp(),
                LocalDataSourceImp(AppDatabase.getInstance(context).getForecastDao()),
                context,
                settingsManager
            ),
            workManager = WeatherWorkManager(context),
            context = context
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            settingsManager = settingsManager,
            weatherRepository = WeatherRepositoryImp.getInstance(
                RemoteDataSourceImp(),
                LocalDataSourceImp(AppDatabase.getInstance(context).getForecastDao()),
                context,
                settingsManager
            ),
            context = context
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
                        },
                        onNavigateToAlerts = {
                            navController.navigate(Routes.NotificationsRoute.toString())
                        },
                        onNavigateToSettings = {
                            navController.navigate(Routes.SettingsRoute.toString())
                        },
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
                        onBackClick = {
                            //                        forecastViewModel.resetLocationFlags()
                            navController.popBackStack()
                        },
                        onSearchClick = {
                            val currentLocation = mapsViewModel.location.value
                            val lat = currentLocation?.lat ?: 30.0444
                            val lon = currentLocation?.lng ?: 31.2357
                            navController.navigate(Routes.MapRoute(lat, lon).toString())
                        },
                        onFavoriteClicked = { location ->
                            forecastViewModel.updateLocation(location)
                            navController.navigate(Routes.HomeRoute.toString()) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Routes.NotificationsRoute.toString()) {
                    NotificationsScreen(
                        onBackClick = {},
                        viewModel = notificationsViewModel,
                    )
                }

                composable(Routes.SettingsRoute.toString()) {
                    SettingsScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        settingsViewModel
                    )
                }
            }
        }
    }

