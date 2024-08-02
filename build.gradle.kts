// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
}

allprojects {
    afterEvaluate {
        if (!project.hasProperty("android")) return@afterEvaluate

        extensions.getByType<com.android.build.gradle.BaseExtension>().apply {
            compileSdkVersion(35)
            defaultConfig {
                minSdk = 26
                targetSdk = 35

                versionCode = 1
                versionName = "1.0"

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
                vectorDrawables {
                    useSupportLibrary = true
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            packagingOptions {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
        }
    }
}
