plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.otivpn.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.otivpn.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
dependencies {
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("com.wireguard.android:tunnel:1.0.20260102")
}
