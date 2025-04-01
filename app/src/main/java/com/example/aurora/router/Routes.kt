package com.example.aurora.router

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes{
    @Serializable
    data object SplashRoute : Routes()

    @Serializable
    data object HomeRoute : Routes()

    @Serializable
    data object FavoritesRoute : Routes()

    @Serializable
    data object SettingsRoute : Routes()

    @Serializable
    data object NotificationsRoute : Routes()

    @Serializable
    data class MapRoute(
        val lat: Double,
        val lon: Double
    ) : Routes() {
        override fun toString(): String = "map/$lat/$lon"
    }

}