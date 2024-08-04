@file:Suppress("UnstableApiUsage")

package com.pedrobneto.plugin.multiplatform

import com.pedrobneto.plugin.util.applyPlugins
import com.pedrobneto.plugin.util.multiplatform
import com.pedrobneto.plugin.util.projectJavaVersionCode
import com.pedrobneto.plugin.util.projectJavaVersionName
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal class InternalBasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.applyPlugins("jetbrains-multiplatform")
        with(target.kotlinExtension) {
            jvmToolchain(projectJavaVersionCode)
        }

        with(target.multiplatform) {
            androidTarget {
                compilations.all { compilation ->
                    compilation.kotlinOptions {
                        jvmTarget = projectJavaVersionName
                    }
                }
            }
        }

        target.plugins.apply("internal-optimize")
    }
}

val NamedDomainObjectContainer<KotlinSourceSet>.androidMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("androidMain")

val NamedDomainObjectContainer<KotlinSourceSet>.androidUnitTest: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("androidUnitTest")

val NamedDomainObjectContainer<KotlinSourceSet>.androidInstrumentedTest: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("androidInstrumentedTest")
