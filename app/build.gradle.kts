plugins {
    id("com.android.application")
}
android {
    namespace = "com.example.oblikdemo"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.oblikdemo"
        minSdk = 26
        targetSdk = 34
        versionCode = 18
        versionName = "1.6-demo"
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
}

