plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")version "2.1.10"
}

android {
    namespace = "com.example.aurora"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.aurora"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.location)
    implementation(libs.androidx.storage)
    implementation(libs.androidx.storage)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation(libs.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.runtime.livedata)
    //Location
    implementation(libs.play.services.location.v2110)
    //Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

//    val nav_version = 2.8.8
    implementation (libs.androidx.navigation.compose)
    implementation (libs.kotlinx.serialization.json)

    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.lottie.compose)

    implementation(libs.compose)

    implementation(libs.places)

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    implementation (libs.gson)

    implementation (libs.material)
    implementation (libs.material3)
    implementation (libs.androidx.swiperefreshlayout)
    implementation(libs.accompanist.swiperefresh)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Dependencies for local unit tests
    testImplementation (libs.junit)
    testImplementation (libs.hamcrest.all)
    testImplementation (libs.androidx.arch.core.core.testing)
    testImplementation (libs.robolectric.robolectric)

    // AndroidX Test - JVM testing
    testImplementation (libs.test.core.ktx)
    //testImplementation "androidx.test.ext:junit:$androidXTestExtKotlinRunnerVersion"

    // AndroidX Test - Instrumented testing
    androidTestImplementation (libs.androidx.core)
    androidTestImplementation (libs.androidx.espresso.core)

    //Timber
//    implementation (libs.timber)

    // hamcrest
    testImplementation (libs.org.hamcrest.hamcrest)
    testImplementation (libs.hamcrest.hamcrest.library)
    androidTestImplementation (libs.org.hamcrest.hamcrest)
    androidTestImplementation (libs.hamcrest.hamcrest.library)


    // AndroidX and Robolectric
    testImplementation (libs.androidx.junit.ktx)
    testImplementation (libs.test.core.ktx)
    testImplementation (libs.robolectric.robolectric)

    // InstantTaskExecutorRule
    testImplementation (libs.androidx.arch.core.core.testing)
    androidTestImplementation (libs.androidx.arch.core.core.testing)

    //kotlinx-coroutines
    implementation (libs.kotlinx.coroutines.android)
    testImplementation (libs.jetbrains.kotlinx.coroutines.test)
    androidTestImplementation (libs.jetbrains.kotlinx.coroutines.test)


    //MockK
    testImplementation (libs.mockk.android)
    testImplementation (libs.mockk.agent)

    implementation (libs.kotlin.test)
}