package com.pedrobneto.plugin.android

import com.pedrobneto.plugin.regularSourceSets
import com.pedrobneto.plugin.setupAndroidLibraryModule
import com.pedrobneto.plugin.util.androidLibrary
import com.pedrobneto.plugin.util.applyPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class InternalLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.applyPlugins("android-library")
        target.plugins.apply("internal-android-base")

        target.setupAndroidLibraryModule()
        target.androidLibrary.regularSourceSets()

        target.plugins.apply("internal-lint")
        target.plugins.apply("internal-test")
        target.plugins.apply("internal-optimize")
    }
}
