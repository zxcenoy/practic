plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.practica"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.practica"
        minSdk = 34
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
}

dependencies {

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("commons-io:commons-io:2.11.0")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}