package com.example.aurora.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

fun Any?.toDoubleOrZero(): Double = when (this) {
    is Number -> this.toDouble()
    is String -> this.toDoubleOrNull() ?: 0.0
    else -> 0.0
}

fun Any?.toIntOrZero(): Int = this.toDoubleOrZero().toInt()

fun Context.hasNetworkConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnected
    }
}