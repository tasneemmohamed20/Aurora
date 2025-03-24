package com.example.aurora

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.example.aurora.home.HomeScreen
import com.example.aurora.home.current_weather.viewmodel.CurrentWeatherViewModel
import com.example.aurora.data.remote.RemoteDataSourceImp
import com.example.aurora.data.repo.WeatherRepositoryImp
import com.example.aurora.router.Routes
import com.example.aurora.ui.theme.babyBlue
import com.example.aurora.ui.theme.babyPurple
import com.example.aurora.ui.theme.darkBabyBlue
import com.example.aurora.ui.theme.darkPurple
import com.example.aurora.ui.theme.gradientBrush
import com.example.aurora.utils.LocationHelper
import com.example.aurora.workers.WeatherWorkManager

class MainActivity : ComponentActivity() {
    private val viewModel: CurrentWeatherViewModel by viewModels {
        CurrentWeatherViewModel.WeatherViewModelFactory(
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
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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

        // Only show bottom nav bar when not on splash screen
        if (currentRoute != Routes.SplashRoute.toString()) {
            CustomBottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                onMapClick = {
                    // Handle map click
                },
                onGpsClick = {
                    // Handle GPS click
                },
                onMenuClick = {
                    // Handle menu click
                }
            )
        }
    }
}

@Composable
fun CustomBottomNavBar(
    modifier: Modifier = Modifier,
    onMapClick: () -> Unit = {},
    onGpsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    var isDarkTheme: Boolean = isSystemInDarkTheme()
    val background =  gradientBrush(isDarkTheme)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Map Icon
            IconButton(onClick = onMapClick) {
                Icon(
                    painter = painterResource(id = R.drawable.navigation), // Replace with actual drawable
                    contentDescription = "Map",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // GPS Icon with Dot
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = onGpsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.gps_svg), // Replace with actual drawable
                        contentDescription = "GPS",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
//                Canvas(
//                    modifier = Modifier
//                        .size(6.dp)
//                        .align(Alignment.TopCenter) // Position above the GPS icon
//                ) {
//                    drawCircle(color = Color.LightGray)
//                }
            }

            // Menu Icon
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(id = R.drawable.menu), // Replace with actual drawable
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

    }
}

