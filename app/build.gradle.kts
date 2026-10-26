plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "fi.iki.ede.fitsviewer"
    compileSdk = 35

    defaultConfig {
        applicationId = "fi.iki.ede.fitsviewer"
        minSdk = 28
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":fitsreader"))
    implementation(project(":imagefuncs"))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.test.junit4.android)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material)

    testImplementation(libs.junit)
}

tasks.configureEach {
    when (name) {
        "assembleDebug" -> {
            dependsOn(
                "testDebugUnitTest",
                ":fitsreader:testDebugUnitTest",
                ":imagefuncs:testDebugUnitTest",
            )
            //task.dependsOn("connectedAndroidTest")   // INSTRUMENTED TESTS, takes long time
        }
    }
}
