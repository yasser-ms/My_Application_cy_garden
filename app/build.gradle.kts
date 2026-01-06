plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Add Firebase plugin
}

android {
    namespace = "com.example.myapplication1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication1"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation ("com.google.firebase:firebase-analytics")

    // WorkManager
    implementation("androidx.work:work-runtime:2.9.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // Camera2
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // 1. MPAndroidChart - For beautiful sensor data charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // 2. Google Play Services Location - For GPS functionality
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 3. Google Play Services Maps (optional - for future map integration)
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // 4. Lifecycle components for LiveData (network monitoring)
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
}