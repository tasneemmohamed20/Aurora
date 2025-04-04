package com.example.aurora

import android.app.Application
import androidx.work.WorkManager
import com.example.aurora.data.local.AppDatabase

class AuroraApplication : Application() {
    override fun onCreate() {
        super.onCreate()

//        AppDatabase.getInstance(
//            this
//        )

        WorkManager .initialize(
            this,
            WorkManager.getInstance(this).configuration
        )
    }
}