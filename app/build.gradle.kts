plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "com.gustate.uotan"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.gustate.uotan"
        minSdk = 31
        targetSdk = 35
        versionCode = 1022
        versionName = "1.0.2.2 Beta"

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

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.animation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.window)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.material)
    implementation(libs.glide)
    implementation(libs.androidx.constraintlayout)

    // ShapeBlurView (https://github.com/centerzx/ShapeBlurView)
    implementation(libs.shapeblurview)

    // Jsoup (https://github.com/jhy/jsoup)
    implementation(libs.jsoup)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.datastore.preferences)

    // OkHttp (https://github.com/square/okhttp)
    implementation(libs.okhttp)



    // SmartRefreshLayout (https://github.com/scwang90/SmartRefreshLayout)
    implementation(libs.refreshlayout)
    implementation(libs.refreshlayout.classicsheader)

    implementation(libs.kotlinx.serialization.json)

    // DialogX (https://github.com/kongzue/DialogX)
    implementation(libs.dialogx)
    implementation(libs.dialogx.materialyou)

    // EasyAndroid (https://github.com/easyandroidgroup/EasyAndroid)
    implementation(libs.easyandroid)

    // Fetch (https://github.com/tonyofrancis/Fetch)
    implementation(libs.fetch)

    implementation(libs.volley)
    implementation(libs.gson)
    implementation(libs.google.flexbox)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}