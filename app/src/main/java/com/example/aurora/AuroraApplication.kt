package com.example.aurora

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.aurora.data.local.AppDatabase

class AuroraApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize database
        AppDatabase.getInstance(this)
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
        )
    }
}