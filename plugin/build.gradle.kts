plugins {
    id("java-gradle-plugin")
    alias(pluginLibraries.plugins.jvm)
}

kotlin { jvmToolchain(17) }

group = "com.pedrobneto.plugin"
version = "1.0.0"

dependencies {
    compileOnly(gradleApi())

    implementation(pluginLibraries.androidx.plugin)
    implementation(pluginLibraries.detekt)
    implementation(pluginLibraries.ktlint)
    implementation(pluginLibraries.jetbrains.plugin)
    implementation(pluginLibraries.jetbrains.extensions)
    implementation(pluginLibraries.jetbrains.kover)
}

sourceSets {
    main {
        java { srcDirs("src/main/java") }
        kotlin { srcDirs("src/main/kotlin") }
    }
}

gradlePlugin {
    plugins {
        //region Android
        create("internal-android-library") {
            id = "internal-android-library"
            displayName = "Internal Library Plugin"
            description =
                "Plug and play for modules those should be a exported library to the world!"
            implementationClass = "com.pedrobneto.plugin.android.InternalLibraryPlugin"
        }

        create("internal-android-sample") {
            id = "internal-android-sample"
            displayName = "Internal Sample Plugin"
            description = "Plug and play for modules to show the world the wonders of tomorrow!"
            implementationClass = "com.pedrobneto.plugin.android.InternalSamplePlugin"
        }

        create("internal-android-base") {
            id = "internal-android-base"
            displayName = "Internal Base Plugin"
            description = "All default config"
            implementationClass = "com.pedrobneto.plugin.android.InternalBasePlugin"
        }
        //endregion

        //region Multiplatform
        create("internal-multiplatform-library") {
            id = "internal-multiplatform-library"
            displayName = "Internal Library Plugin"
            description =
                "Plug and play for modules those should be a exported library to the world!"
            implementationClass = "com.pedrobneto.plugin.multiplatform.InternalLibraryPlugin"
        }

        create("internal-multiplatform-sample") {
            id = "internal-multiplatform-sample"
            displayName = "Internal Sample Plugin"
            description = "Plug and play for modules to show the world the wonders of tomorrow!"
            implementationClass = "com.pedrobneto.plugin.multiplatform.InternalSamplePlugin"
        }

        create("internal-multiplatform-base") {
            id = "internal-multiplatform-base"
            displayName = "Internal Base Plugin"
            description = "All default config"
            implementationClass = "com.pedrobneto.plugin.multiplatform.InternalBasePlugin"
        }
        //endregion

        //region Generic
        create("internal-optimize") {
            id = "internal-optimize"
            displayName = "Internal Optimization Plugin"
            description = "Optimize dependencies"
            implementationClass =
                "com.pedrobneto.plugin.InternalOptimizeDependenciesAndFilterTasksPlugin"
        }

        create("internal-lint") {
            id = "internal-lint"
            displayName = "Internal Lint Plugin"
            description = "Enables and configure lint for module"
            implementationClass = "com.pedrobneto.plugin.InternalLintPlugin"
        }

        create("internal-test") {
            id = "internal-test"
            displayName = "Internal Test Plugin"
            description = "Enables and configure test for module"
            implementationClass = "com.pedrobneto.plugin.InternalTestPlugin"
        }

        create("internal-group") {
            id = "internal-group"
            displayName = "Internal Group Plugin"
            description = "Enables and configure group for module"
            implementationClass = "com.pedrobneto.plugin.InternalGroupPlugin"
        }

        create("internal-publish") {
            id = "internal-publish"
            displayName = "Internal Publish Plugin"
            description = "Enables and configure publish for module"
            implementationClass = "com.pedrobneto.plugin.InternalPublishPlugin"
        }
        //endregion
    }
}
