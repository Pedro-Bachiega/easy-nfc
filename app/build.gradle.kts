plugins {
    id("internal-android-sample")
}

android {
    namespace = "com.pedrobneto.sample.easynfc"

    defaultConfig {
        applicationId = "com.pedrobneto.sample.easynfc"
    }
}

dependencies {
    implementation(project(":easy-nfc"))

    implementation(libraries.androidx.appcompat)
    implementation(libraries.androidx.constraintlayout)
    implementation(libraries.androidx.core.ktx)
    implementation(libraries.androidx.lifecycle.livedata)
    implementation(libraries.androidx.lifecycle.process)
    implementation(libraries.androidx.lifecycle.runtime)
}