plugins {
    id("com.android.application")
    id("kotlin-android")
    id ("kotlin-kapt")
    id ("androidx.navigation.safeargs.kotlin")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

android {
    namespace = "com.onlinepayments.client.kotlin.exampleapp.xml"

    defaultConfig {
        applicationId = "com.onlinepayments.client.kotlin.exampleapp.xml"
        minSdk = 21
        compileSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        abortOnError = false
        lintConfig = file("lint.xml")
    }
}

dependencies {
    // Project
    implementation(project(":common"))

    // Kotlin
    val coroutinesVersion = rootProject.extra["coroutines_version"]
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // GSON
    val gsonVersion = rootProject.extra["gson_version"]
    implementation("com.google.code.gson:gson:$gsonVersion")

    // Android X
    val coreVersion = rootProject.extra["core_version"]
    implementation("androidx.core:core-ktx:$coreVersion")
    val appCompatVersion = rootProject.extra["appCompat_version"]
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    val navigationVersion = rootProject.extra["navigation_version"]
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    val dataStoreVersion = rootProject.extra["data_store_version"]
    implementation("androidx.datastore:datastore:$dataStoreVersion")
    implementation("androidx.datastore:datastore-preferences-rxjava2:$dataStoreVersion")
    implementation("androidx.datastore:datastore-preferences-rxjava3:$dataStoreVersion")
    val kotlinSerializationVersion = rootProject.extra["kotlin_serialization_version"]
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    // UI
    val materialVersion = rootProject.extra["material_version"]
    implementation("com.google.android.material:material:$materialVersion")
    // The following dependency gives a false suggestion that a newer version is available,
    // 2.8 is the most recent version.
    //noinspection GradleDependency
    implementation("com.squareup.picasso:picasso:2.8")
    implementation(files("libs/stepindicator-release.aar"))

    // Google Pay
    val googlePayVersion = rootProject.extra["googlePay_version"]
    implementation("com.google.android.gms:play-services-wallet:$googlePayVersion")

    // OnlinePayments SDK
    val onlinePaymentsSDKVersion = rootProject.extra["onlinePaymentsSDK_version"]
    implementation("com.worldline-solutions:sdk-client-android:$onlinePaymentsSDKVersion")
}
