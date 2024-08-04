@file:Suppress("UnstableApiUsage")

package com.pedrobneto.plugin.android

import com.pedrobneto.plugin.util.applyPlugins
import com.pedrobneto.plugin.util.projectJavaVersionCode
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

internal class InternalBasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.applyPlugins("jetbrains-kotlin")
        with(target.kotlinExtension) {
            jvmToolchain(projectJavaVersionCode)
        }
    }
}
