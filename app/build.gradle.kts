plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.learnning.cameraxdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.learnning.cameraxdemo"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //CameraX核心库，提供CameraX API
    implementation("androidx.camera:camera-core:1.2.3")
    //实现了CameraX与Comero2 API的适配器
    implementation("androidx.camera:camera-camera2:1.2.3")
    //处理Camerax 与 Activity/Fragment生命周期的绑定
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    //提供了录制视频的 API
    implementation("androidx.camera:camera-video:1.2.3")
    //用于预览照片的视图
    implementation("androidx.camera:camera-view:1.2.3")
    //一些额外功能，如人脸检测
    implementation("androidx.camera:camera-extensions:1.2.3")

}