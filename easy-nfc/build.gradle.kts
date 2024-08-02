plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.pedrobneto.easynfc"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.material)
    testImplementation(libs.junit)
}