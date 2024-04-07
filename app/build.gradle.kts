plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.trashinformation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.trashinformation"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    //added by me
    aaptOptions {
        noCompress +="WasteClassificationModel.tflite"
        //noCompress("WasteClassificationModel.tflite")
        // or noCompress "lite"
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.engage.core)
    //implementation(libs.image.labeling.default.common)
    // testImplementation(libs.junit)
    //androidTestImplementation(libs.ext.junit)
    //androidTestImplementation(libs.espresso.core)

    implementation ("androidx.recyclerview:recyclerview:VERSION")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation(libs.image.labeling.default.common)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //ml
    // implementation("com.google.mlkit:image-labeling-custom:17.0.2")

    //google ml
    implementation("com.google.mlkit:image-labeling:17.0.8")



}