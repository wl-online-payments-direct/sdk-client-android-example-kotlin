plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

android {
    namespace = "com.onlinepayments.client.kotlin.exampleapp.compose"

    defaultConfig {
        applicationId = "com.onlinepayments.client.kotlin.exampleapp.compose"
        minSdk = 21
        compileSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = false
        lintConfig = file("lint.xml")
        disable += "ObsoleteLintCustomCheck"
    }
}

dependencies {
    // Project
    implementation((project(":common")))

    // Kotlin
    val coroutinesVersion = rootProject.extra["coroutines_version"]
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // GSON
    val gsonVersion = rootProject.extra["gson_version"]
    implementation("com.google.code.gson:gson:$gsonVersion")

    // Android X
    val coreVersion = rootProject.extra["core_version"]
    implementation("androidx.core:core-ktx:$coreVersion")
    val lifecycleVersion = rootProject.extra["lifecycle_version"]
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycleVersion")
    val dataStoreVersion = rootProject.extra["data_store_version"]
    implementation("androidx.datastore:datastore:$dataStoreVersion")
    implementation("androidx.datastore:datastore-preferences-rxjava2:$dataStoreVersion")
    implementation("androidx.datastore:datastore-preferences-rxjava3:$dataStoreVersion")
    val kotlinSerializationVersion = rootProject.extra["kotlin_serialization_version"]
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    // UI
    val materialVersion = rootProject.extra["material_version"]
    implementation("com.google.android.material:material:$materialVersion")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Jetpack Compose
    val composeVersion = rootProject.extra["compose_version"]
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    val composeMaterialVersion = rootProject.extra["compose_material_version"]
    implementation("androidx.compose.material:material:$composeMaterialVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeMaterialVersion")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.34.0")
    
    // Google pay
    val googlePayVersion = rootProject.extra["googlePay_version"]
    implementation("com.google.android.gms:play-services-wallet:$googlePayVersion")

    // OnlinePayments SDK
    val onlinePaymentsSDKVersion = rootProject.extra["onlinePaymentsSDK_version"]
    implementation("com.worldline-solutions:sdk-client-android:$onlinePaymentsSDKVersion")
}
