plugins {
//    kotlin
    `android-library`
//    id("com.android.library")
    `kotlin-kapt`
    id("com.huawei.agconnect")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("com.huawei.hms:location:5.1.0.303")
    implementation("com.google.android.gms:play-services-location:19.0.1")
    //implementation("com.huawei.hms:hwid:5.0.0.300")

    implementation("com.rocket.android.core:crash-reporting-android:0.0-beta0")
    implementation("com.rocket.android.core:permissions:0.0.0-alpha01")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("com.rocket.android.core:core-data-network:0.0-beta0")
    implementation("com.rocket.core:core-domain:0.0-beta0")

    //implementation("com.google.android.gms:play-services:12.0.1")

    implementation("org.jetbrains.kotlin:kotlin-test:1.5.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
}
