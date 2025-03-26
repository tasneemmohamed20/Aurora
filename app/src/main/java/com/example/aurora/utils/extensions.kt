package com.example.aurora.utils

fun Any?.toDoubleOrZero(): Double = when (this) {
    is Number -> this.toDouble()
    is String -> this.toDoubleOrNull() ?: 0.0
    else -> 0.0
}

fun Any?.toIntOrZero(): Int = this.toDoubleOrZero().toInt()