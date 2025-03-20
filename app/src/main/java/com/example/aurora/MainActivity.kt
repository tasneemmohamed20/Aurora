package com.example.aurora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aurora.router.Routes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppRoutes { isSplashScreen ->
                if (!isSplashScreen) {
                    enableEdgeToEdge()
                }
            }
        }
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