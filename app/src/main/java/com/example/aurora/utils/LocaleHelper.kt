package com.example.aurora.utils

import android.content.Context
import android.os.Build
import android.view.View
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = context.resources.configuration.apply {
            setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                setLayoutDirection(locale)
            }
        }

        // Update configuration and create new context
        val newContext = context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Force RTL/LTR at system level
        val direction = if (language == "ar") {
            View.LAYOUT_DIRECTION_RTL
        } else {
            View.LAYOUT_DIRECTION_LTR
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            newContext.resources.configuration.setLayoutDirection(locale)
        }

        return newContext
    }

    fun isRTL(locale: Locale = Locale.getDefault()): Boolean {
        return TextUtilsCompat.getLayoutDirectionFromLocale(locale) == ViewCompat.LAYOUT_DIRECTION_RTL
    }
}