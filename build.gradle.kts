// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra.apply {
        set("core_version", "1.13.1")
        set("appCompat_version", "1.6.1")
        set("material_version", "1.12.0")
        set("lifecycle_version", "2.8.0")
        set("navigation_version", "2.7.7")
        set("compose_version", "1.6.7")
        set("compose_material_version", "1.6.2")
        set("coroutines_version", "1.8.0")
        set("googlePay_version", "19.3.0")
        set("onlinePaymentsSDK_version", "4.1.0")
        set("gson_version", "2.10.1")
        set("data_store_version", "1.1.1")
        set("kotlin_serialization_version", "1.6.3")
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        val navigationVersion = rootProject.extra["navigation_version"]
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

task("clean") {
    delete(rootProject.layout.buildDirectory)
}
