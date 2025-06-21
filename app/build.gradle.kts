plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
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

            buildConfigField("String", "CAREER_NET_API_URL", "\"https://www.career.go.kr/cnet/openapi/\"")
            buildConfigField("String", "CAREER_NET_OPENAPI_URL", "\"https://www.career.go.kr/cnet/openapi/getOpenApi\"")
            buildConfigField("String", "CAREER_NET_API_KEY", "\"f2f6128e7cab84a695ad3ff381ff6b04\"")
            buildConfigField("String", "SERVER_BASE_URL", "\"http://hide-ipv4.xyz/api/\"")
        }

        debug {
            buildConfigField("String", "CAREER_NET_API_URL", "\"https://www.career.go.kr/cnet/openapi/\"")
            buildConfigField("String", "CAREER_NET_OPENAPI_URL", "\"https://www.career.go.kr/cnet/openapi/getOpenApi\"")
            buildConfigField("String", "CAREER_NET_API_KEY", "\"f2f6128e7cab84a695ad3ff381ff6b04\"")
            buildConfigField("String", "SERVER_BASE_URL", "\"http://hide-ipv4.xyz/api/\"")
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Calendar
    implementation("ru.cleverpumpkin:crunchycalendar:2.5.0")
    implementation(libs.androidx.compose.material)

    // Java 8+ API desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // 테스트 의존성
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    // 마크다운
    implementation ("io.noties.markwon:core:4.6.2")
    implementation ("io.noties.markwon:editor:4.6.2")
    implementation ("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation ("io.noties.markwon:ext-tables:4.6.2")
    implementation ("io.noties.markwon:html:4.6.2")
    implementation ("io.noties.markwon:image:4.6.2")
    implementation ("io.noties.markwon:linkify:4.6.2")
}