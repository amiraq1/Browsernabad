plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.nabdh.browser"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nabdh.browser"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // يتم تحميل المفتاح الآن عبر buildTypes

    }

    buildTypes {
        debug {
            buildConfigField("String", "GEMINI_API_KEY", project.findProperty("GEMINI_API_KEY") as String? ?: "\"\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "GEMINI_API_KEY", project.findProperty("GEMINI_API_KEY") as String? ?: "\"\"")
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
        compose = true
        viewBinding = false
        buildConfig = true // تفعيل BuildConfig
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    // حل مشاكل التعارض في ملفات المكتبات
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // المحرك
    implementation(libs.geckoview)
    implementation(libs.kotlinx.coroutines.android)

    // الذكاء الاصطناعي
    implementation(libs.google.generative.ai)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    debugImplementation(libs.androidx.ui.tooling)
}
