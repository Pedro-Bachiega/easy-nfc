@file:Suppress("UnstableApiUsage")

package com.pedrobneto.plugin

import com.pedrobneto.plugin.util.allDefinedDependencies
import com.pedrobneto.plugin.util.applicationComponent
import com.pedrobneto.plugin.util.libraries
import com.pedrobneto.plugin.util.libraryComponent
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class InternalOptimizeDependenciesAndFilterTasksPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val libraries = target.libraries
        val allDefinedLibraries = libraries.allDefinedDependencies
        target.configurations.configureEach { config ->
            config.resolutionStrategy { strategy ->
                strategy.failOnVersionConflict()
                strategy.preferProjectModules()

                strategy.setForcedModules(allDefinedLibraries)
            }
        }
        val component = kotlin.runCatching { target.libraryComponent }.getOrNull()
            ?: kotlin.runCatching { target.applicationComponent }.getOrNull()
        component?.finalizeDsl {
            target.tasks.configureEach { task ->
                val isRelease = task.name.contains("release", true)
                val isLint = task.name.contains("lint", true)
                val isTest = task.name.contains("test", true)
                val isKover = task.name.contains("kover", true)
                val mustDisable = isLint || isTest || isKover
                if (isRelease && mustDisable) {
                    task.enabled = false
                    task.group = "z-disabled"
                }
            }
        }
    }
}
