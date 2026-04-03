import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.uotan.forum"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.uotan.forum"
        minSdk = 28
        targetSdk = 36
        versionCode = 1041
        versionName = "1.0.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 定义密钥字段
        val localPropertiesFile = rootProject.file("local.properties")
        val localProperties = Properties().apply {
            if (localPropertiesFile.exists()) {
                load(localPropertiesFile.inputStream())
            }
        }
        val xfApiKey = localProperties.getProperty("xf.api.key") ?: "\"MISSING_API_KEY\""
        buildConfigField("String", "xfApiKey", xfApiKey)
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.animation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.window)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.cardview)
    implementation(libs.material3)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // compose
    // 使用 BOM (Bill of Materials) 统一管理 Compose 库版本
    val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose 基础依赖 (必需)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview) // 预览支持
    debugImplementation(libs.compose.ui.tooling) // 调试工具
    implementation(libs.androidx.activity.compose) // Activity 集成
    implementation(libs.androidx.navigation.compose)

    // Material Design 依赖 (选择其一)
    implementation(libs.androidx.material3) // Material Design 3
    implementation(libs.androidx.material.icons.extended)

    // 可选功能依赖
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel 集成
    implementation(libs.androidx.runtime.livedata) // LiveData 集成
    testImplementation(libs.androidx.ui.test.junit4) // UI 测试
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.capsule)

    implementation(libs.material)
    implementation(libs.glide)
    implementation(libs.androidx.constraintlayout)

    // Better UI (๑•̀ㅂ•́)و✧
    // ShapeBlurView (https://github.com/centerzx/ShapeBlurView)
    implementation(libs.shapeblurview)
    // HazeBlur (https://github.com/chrisbanes/haze Apache-2.0)
    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Jsoup (https://github.com/jhy/jsoup)
    implementation(libs.jsoup)

    implementation(libs.compose.webview)

    implementation(libs.androidliquidglass)


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
    implementation(libs.logging.interceptor)

    // SmartRefreshLayout (https://github.com/scwang90/SmartRefreshLayout)
    implementation(libs.refreshlayout)
    implementation(libs.refreshlayout.classicsheader)
    implementation(libs.refreshlayout.classicsfooter)

    implementation(libs.kotlinx.serialization.json)

    // DialogX (https://github.com/kongzue/DialogX)
    implementation(libs.dialogx)
    implementation(libs.dialogx.materialyou)

    // Fetch (https://github.com/tonyofrancis/Fetch)
    implementation(libs.fetch)

    implementation(libs.volley)
    implementation(libs.gson)
    implementation(libs.google.flexbox)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.runtime)

    implementation(libs.imageviewer)

    implementation(libs.shadowlayout)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    implementation(libs.coil.compose)

    implementation(libs.exviewpagerbottomsheet)

    /**
     * JiaGuZhuangZhi Libraries
     * 欸嘿！~ 小孟自己的哦~
     */
    implementation(libs.gustate.appbar)
}