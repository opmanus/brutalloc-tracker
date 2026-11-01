plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.brutal.tracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.brutal.tracker"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: "/home/arka/.android/brutaltrack-release.keystore"
            val ksFile = file(keystorePath)
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "ArkaMandalKey2026"
                keyAlias = System.getenv("KEY_ALIAS") ?: "brutaltrack"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "ArkaMandalKey2026"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Location & Services
    implementation(libs.play.services.location)
    
    // MQTT & Networking
    implementation(libs.paho.mqtt)
    implementation(libs.gson)
    
    // Security & Encrypted Preferences
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)
}
