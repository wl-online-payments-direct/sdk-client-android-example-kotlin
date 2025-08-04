@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.android") version "2.0.0"
    }
}

rootProject.name = "OnlinePaymentsExample"
include(":compose")
include(":common")
include(":xml")
