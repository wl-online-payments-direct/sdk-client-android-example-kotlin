plugins {
    id("com.android.library")
    id("kotlin-android")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

android {
    namespace = "com.onlinepayments.client.kotlin.exampleapp.common"

    defaultConfig {
        minSdk = 21
        compileSdk = 34
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "LOGGING_ENABLED", "true")
        }
        // To disable logging in production,
        // you can set the LOGGING_ENABLED field to false in a release build of your app
        /*
        release {
            buildConfigField "Boolean", "LOGGING_ENABLED", 'false'
        }
        */
    }

    lint {
        abortOnError = false
        lintConfig = file("lint.xml")
    }
}

dependencies {
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

    // Google pay
    val googlePayVersion = rootProject.extra["googlePay_version"]
    implementation("com.google.android.gms:play-services-wallet:$googlePayVersion")

    // OnlinePayments SDK
    val onlinePaymentsSDKVersion = rootProject.extra["onlinePaymentsSDK_version"]
    implementation("com.worldline-solutions:sdk-client-android:$onlinePaymentsSDKVersion")
}
