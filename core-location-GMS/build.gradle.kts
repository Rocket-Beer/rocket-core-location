plugins {
    id("com.android.library")
    kotlin("android")
}
android {
    compileSdkVersion(apiLevel = 30)

    defaultConfig {
        minSdkVersion(minSdkVersion = 21)
        targetSdkVersion(targetSdkVersion = 30)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(name = "proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.20")
    implementation("com.google.android.gms:play-services-location:19.0.1")

    api("com.rocket.android.core:crash-reporting-android:0.0-beta0")
    api("com.rocket.android.core:permissions:0.0.1-alpha02-SNAPSHOT")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("com.rocket.android.core:core-data-network:0.0-beta0")
    implementation("com.rocket.core:core-domain:0.0-beta0")

    implementation("org.jetbrains.kotlin:kotlin-test:1.5.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("io.mockk:mockk-android:1.11.0")
}
