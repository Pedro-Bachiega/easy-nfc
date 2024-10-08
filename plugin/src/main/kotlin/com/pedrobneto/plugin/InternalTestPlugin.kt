@file:Suppress("UnstableApiUsage")

package com.pedrobneto.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.pedrobneto.plugin.util.androidApplication
import com.pedrobneto.plugin.util.androidLibrary
import com.pedrobneto.plugin.util.applyPlugins
import com.pedrobneto.plugin.util.jacoco
import com.pedrobneto.plugin.util.kover
import com.pedrobneto.plugin.util.koverReport
import com.pedrobneto.plugin.util.libraries
import com.pedrobneto.plugin.util.version
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class InternalTestPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.plugins.apply("jacoco")
        target.applyPlugins("jetbrains-kover")

        // Kover configuration
        with(target.kover) {
            if (target.plugins.hasPlugin("android-application")) {
                disable()
            }
        }
        with(target.koverReport) {
            defaults { reports ->
                reports.mergeWith("debug")
            }
        }

        // Kover configuration
        with(target.jacoco) { toolVersion = target.libraries.version("jacoco") }

        // Regular Test configuration
        kotlin.runCatching { setForApplication(target.androidApplication) }
        kotlin.runCatching { setForLibrary(target.androidLibrary) }
    }

    private fun setForApplication(android: ApplicationExtension) = with(android) {
        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes.maybeCreate("debug").apply {
            enableAndroidTestCoverage = false
            enableUnitTestCoverage = false
        }
        buildTypes.maybeCreate("release").apply {
            enableAndroidTestCoverage = false
            enableUnitTestCoverage = false
        }

        testOptions {
            unitTests.all { it.enabled = false }
            unitTests.isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
            animationsDisabled = true
        }
    }

    private fun setForLibrary(android: LibraryExtension) = with(android) {
        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes.maybeCreate("debug").apply {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
        buildTypes.maybeCreate("release").apply {
            enableAndroidTestCoverage = false
            enableUnitTestCoverage = false
        }

        testOptions {
            unitTests.isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
            animationsDisabled = true
        }
    }
}
