package com.pedrobneto.plugin

import com.pedrobneto.plugin.util.applyPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.com.google.gson.JsonArray
import java.nio.file.Files

internal class InternalGroupPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.evaluationDependsOnChildren()
        target.applyPlugins("jetbrains-kover")

        // Try to unify coverage reports
        target.subprojects.onEach {
            target.dependencies.add("kover", it)
        }

        // Generate file containing all modules with publish plugin attached
        target.tasks.register("publishModules") { task ->
            task.group = "groupTask"
            // Store target directory into a variable to avoid project reference in the configuration cache
            val directory = target.layout.buildDirectory.get()
            val file = directory.file("modules.txt").asFile
            val publishLibraries = target.subprojects
                .filter { it.plugins.hasPlugin("internal-publish") }
                .map { it.name }
            task.doLast { _ ->
                Files.createDirectories(directory.asFile.toPath())
                val json = JsonArray()
                publishLibraries.onEach(json::add)

                if (file.exists().not()) {
                    file.createNewFile()
                }
                file.writeText(json.toString())
                println("Publish module list generated at: $file")
                println(json.toString())
            }
        }
    }
}
