plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val backendEnvironment = providers.gradleProperty("respondoBackendEnvironment")
    .orElse(providers.environmentVariable("RESPONDO_BACKEND_ENVIRONMENT"))
    .orElse("development")
    .get()
    .lowercase()
require(backendEnvironment in setOf("development", "staging", "production")) {
    "respondoBackendEnvironment must be development, staging, or production"
}
val backendUrlProperty = "respondo${backendEnvironment.replaceFirstChar { it.uppercase() }}BackendUrl"
val backendUrlEnvironment = "RESPONDO_${backendEnvironment.uppercase()}_BACKEND_URL"
val backendBaseUrl = providers.gradleProperty(backendUrlProperty)
    .orElse(providers.environmentVariable(backendUrlEnvironment))
    .orElse("")
    .get()
    .trim()
if (backendBaseUrl.isNotEmpty()) {
    val parsedUrl = java.net.URI(backendBaseUrl)
    require(parsedUrl.scheme == "https" && parsedUrl.host != null && parsedUrl.userInfo == null &&
        parsedUrl.query == null && parsedUrl.fragment == null && backendBaseUrl.endsWith("/")) {
        "Configured backend URL must be an HTTPS origin/base path ending in /, without credentials, query, or fragment"
    }
}

android {
    namespace = "com.respondo.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.respondo.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "RESPONDO_BACKEND_ENVIRONMENT", "\"$backendEnvironment\"")
        buildConfigField("String", "RESPONDO_BACKEND_BASE_URL", "\"$backendBaseUrl\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Compose 1.12 requires compileSdk 37 and AGP 9; this project targets API 36 on AGP 8.13.
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-saveable")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.room:room-testing:2.7.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.1")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.room:room-testing:2.7.2")
}
