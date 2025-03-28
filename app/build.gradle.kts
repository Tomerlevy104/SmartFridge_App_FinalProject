plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.smartfridge_app_finalproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smartfridge_app_finalproject"
        minSdk = 26
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.google.android.libraries.places:places:4.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")


    //Maps
    implementation(libs.gms.maps)
    implementation(libs.gms.location)

    //Firebase:
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    //Firebase AuthUi:
    implementation(libs.firebase.ui.auth)

    //Glide - for image loader
    implementation(libs.glide)

    //Firestore:
    implementation(libs.firebase.firestore)

    //Storage
    implementation(libs.firebase.storage)

    //Realtime DB:
    implementation(libs.firebase.database)

    // ML Kit לסריקת ברקודים
    implementation ("com.google.mlkit:barcode-scanning:17.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")


}