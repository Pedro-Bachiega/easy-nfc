package com.pedrobneto.plugin

import com.android.build.api.dsl.LibraryExtension
import com.pedrobneto.plugin.util.androidLibrary
import com.pedrobneto.plugin.util.libraries
import com.pedrobneto.plugin.util.version
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog

internal fun Project.setupAndroidLibraryModule() = with(androidLibrary) {
    // Common Setup
    commonSetup()

    // Setup Android Version support
    setupVersion(libraries)

    // Exclusive Library Configurations
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

private fun LibraryExtension.setupVersion(libraries: VersionCatalog) {
    compileSdk = libraries.version("build-sdk-compile").toInt()
    buildToolsVersion = libraries.version("build-tools")

    defaultConfig {
        minSdk = libraries.version("build-sdk-min").toInt()
    }
}
