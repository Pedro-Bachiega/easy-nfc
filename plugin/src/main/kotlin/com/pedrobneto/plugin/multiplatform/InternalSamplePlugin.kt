package com.pedrobneto.plugin.multiplatform

import com.pedrobneto.plugin.setupAndroidApplicationModule
import com.pedrobneto.plugin.util.applyPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class InternalSamplePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.applyPlugins("android-application")
        target.plugins.apply("internal-multiplatform-base")

        target.setupAndroidApplicationModule()

        target.plugins.apply("internal-lint")
        target.plugins.apply("internal-test")
        target.plugins.apply("internal-optimize")
    }
}