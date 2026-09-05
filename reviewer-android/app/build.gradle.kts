plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
 id("org.jetbrains.kotlin.plugin.serialization")
}

android {
 namespace="com.usermonitor.reviewer"
 compileSdk=35
 defaultConfig { applicationId="com.usermonitor.reviewer"; minSdk=26; targetSdk=35; versionCode=1; versionName="1.0.0" }
 buildFeatures { compose=true; buildConfig=true }
 buildTypes { release { isMinifyEnabled=true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro") } }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17"; freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api" }
}

dependencies {
 implementation(platform("androidx.compose:compose-bom:2026.08.00"))
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
 implementation("io.ktor:ktor-client-core:3.0.1")
 implementation("io.ktor:ktor-client-okhttp:3.0.1")
 implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
 implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
 debugImplementation("androidx.compose.ui:ui-tooling")
}