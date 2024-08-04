pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        register("libraries") {
            from(files("$rootDir/gradle/libs.versions.toml"))
        }
        register("pluginLibraries") {
            from(files("$rootDir/gradle/plugin.versions.toml"))
        }
    }
}

rootProject.name = "Easy Nfc"

// Plugins
includeBuild("plugin")

include(":app")
include(":easy-nfc")
