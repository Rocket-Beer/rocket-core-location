plugins {
    kotlin
    id("java-library")
    kotlin("kapt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("com.huawei.hms:location:5.1.0.303")
    implementation("com.rocket.core:crash-reporting:0.0-beta0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("com.rocket.android.core:core-data-network:0.0-beta0")
    implementation("com.rocket.core:core-domain:0.0-beta0")

    api("org.jetbrains.kotlin:kotlin-test:1.5.20")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
}
