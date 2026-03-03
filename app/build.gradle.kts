plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.proyectofinal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.proyectofinal"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Librería para hacer peticiones a Supabase
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
// Librería opcional para manejar los datos JSON que envía Supabase
    implementation("com.google.code.gson:gson:2.10.1")

    // (modificado) Dependencias de Gemini y Guava copiadas del proyecto pantalla Inicio
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.guava:guava:31.1-android")

    // (modificado) Dependencias copiadas de la App de Pájaros
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}