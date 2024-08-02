plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.pedrobneto.sample.easynfc"

    defaultConfig {
        applicationId = "com.pedrobneto.sample.easynfc"
    }
}

dependencies {
    implementation(project(":easy-nfc"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime)
}