plugins {
    id("internal-android-library")
    id("internal-publish")
}

android {
    namespace = "com.pedrobneto.easynfc"
}

dependencies {
    implementation(libraries.androidx.appcompat)
    implementation(libraries.androidx.core.ktx)
    implementation(libraries.androidx.lifecycle.livedata)
    implementation(libraries.androidx.lifecycle.process)
    implementation(libraries.androidx.lifecycle.runtime)
    implementation(libraries.material)
    testImplementation(libraries.junit)
}