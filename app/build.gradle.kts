plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.gustate.uotan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gustate.uotan"
        minSdk = 28
        targetSdk = 35
        versionCode = 1019
        versionName = "1.0.1.9 Beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.window)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.material.v1120)
    implementation(libs.com.github.bumptech.glide.glide)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.shapeblurview)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.okhttp)
    implementation(libs.androidx.core.animation)
    implementation(libs.io.github.scwang90.refresh.layout.kernel3)      //核心必须依赖
    implementation(libs.refresh.header.classics)    //经典刷新头
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}