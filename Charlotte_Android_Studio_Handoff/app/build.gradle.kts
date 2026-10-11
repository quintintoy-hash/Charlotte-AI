plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android { namespace = "com.charlotte.voice"; compileSdk = 35
    defaultConfig { applicationId = "com.charlotte.voice"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.2.0" }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.core:core-ktx:1.15.0")
    testImplementation(kotlin("test"))
}
