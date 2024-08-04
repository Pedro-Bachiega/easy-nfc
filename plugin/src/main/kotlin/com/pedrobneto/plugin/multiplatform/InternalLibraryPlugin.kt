package com.pedrobneto.plugin.multiplatform

import com.pedrobneto.plugin.setupAndroidLibraryModule
import com.pedrobneto.plugin.util.applyPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class InternalLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.applyPlugins("android-library")
        target.setupAndroidLibraryModule()

        target.plugins.apply("internal-multiplatform-base")
        target.plugins.apply("internal-lint")
        target.plugins.apply("internal-test")
    }
}
