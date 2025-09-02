plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.alisteners.pengaduan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alisteners.pengaduan"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    implementation(libs.play.services.location)
    implementation(libs.exifinterface)
    implementation(libs.retrofit) // Versi stabil Retrofit
    implementation(libs.converter.gson) // Converter untuk JSON (Gson)
    implementation(libs.okhttp) // OkHttp (biasanya ditarik oleh Retrofit, tapi baik untuk eksplisit)
    implementation(libs.logging.interceptor)
    implementation(libs.swiperefreshlayout) // (Opsional) Untuk logging request/response HTTP
    implementation(libs.glide)
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.vanniktech:android-image-cropper:4.6.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("com.github.vipulasri:timelineview:1.2.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}