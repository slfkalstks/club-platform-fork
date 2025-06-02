plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize") // 첫 번째 파일에서 가져온 plugin
    id("kotlin-kapt") // 데이터 바인딩을 위한 kapt 추가
}

android {
    namespace = "kc.ac.uc.clubplatform"
    compileSdk = 35

    defaultConfig {
        applicationId = "kc.ac.uc.clubplatform"
        minSdk = 27
        targetSdk = 35
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

    buildFeatures {
        viewBinding = true
        dataBinding = true // 데이터 바인딩 활성화
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // 코어 라이브러리 디슈가링 활성화
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}



dependencies {
    // 기본 AndroidX 의존성
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Retrofit 의존성
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // XML 대신 JSON 변환기 사용
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // 사용하지 않는 라이브러리 주석 처리 (일시적으로)
     implementation("ru.cleverpumpkin:crunchycalendar:2.5.0")
    implementation(libs.androidx.compose.material)

    // Java 8+ API desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // 테스트 의존성
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
