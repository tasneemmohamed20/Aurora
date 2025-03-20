package com.example.aurora.router

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes{
    @Serializable
    data object SplashRoute : Routes()

    @Serializable
    data object HomeRoute : Routes()

}