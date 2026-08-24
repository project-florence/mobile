plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.florence.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.florence.app"
        minSdk = 26
        targetSdk = 36
        // Version policy: versionCode/versionName are the single source of truth for
        // app releases. versionName mirrors the Git release tag (e.g. tag "v0.7.0" →
        // versionName "0.7.0") so web/desktop and mobile stay aligned; bump both together
        // at release time. versionCode stays 1 for the first release and increments by 1
        // for every subsequent release (Android requires strictly increasing ints).
        versionCode = 1
        versionName = "0.7.0"
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:7055/\"")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "API_BASE_URL", "\"https://api.florencex.com.tr/\"")
        }
    }

    buildTypes {
        release {
            // NOTE: release APKs are intentionally built UNsigned. Signing is done
            // out-of-band by the developer with apksigner (keystore is NOT checked
            // into the repo); see docs/release-signing.md.
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.security.crypto)
    implementation(libs.appcompat)
    // Coil 3: async image loading + SVG decoding for backend-served avatars.
    // coil-svg registers SvgDecoder and coil-network-okhttp registers OkHttpNetworkFetcher
    // (both auto-discovered by Coil's default ImageLoader via ServiceLoader -> R8-consumer
    // rules keep the registrations for the minified release build).
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.network.okhttp)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
