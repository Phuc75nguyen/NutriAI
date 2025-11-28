plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nutriai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nutriai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // THAY THẾ BẰNG CÁC DÒNG 1.4.0 SAU:
    implementation ("androidx.camera:camera-camera2:1.4.0")
    implementation ("androidx.camera:camera-lifecycle:1.4.0") //
    implementation ("androidx.camera:camera-view:1.4.0")     //
    implementation ("androidx.camera:camera-video:1.4.0")   //
    implementation ("androidx.camera:camera-core:1.4.0")     //

    implementation("com.github.hadibtf:SemiCircleArcProgressBar:1.1.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.material:material:1.12.0")

    // thêm thư viện glide cho ảnh gif
    implementation("com.github.bumptech.glide:glide:4.15.1") // [cite: 4]
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1") // [cite: 4]

    // retrofit để gọi api
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
<<<<<<< HEAD
    // Markwon: Thư viện render Markdown siêu nhẹ cho Android
    implementation("io.noties.markwon:core:4.6.2")


=======
    
    // Markwon for Markdown rendering
    implementation("io.noties.markwon:core:4.6.2")
>>>>>>> 26670c1 (update UI Lucfin markdown image)

}