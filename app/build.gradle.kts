plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.welo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.welo"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation ("com.google.android.libraries.places:places:3.3.0")
    // https://mvnrepository.com/artifact/org.osmdroid/osmdroid-shape
    implementation("org.osmdroid:osmdroid-shape:6.1.20")
    implementation("com.caverock:androidsvg:1.4")
    implementation(libs.play.services.maps)
    implementation("com.graphhopper:graphhopper-core:9.0")
    implementation("com.github.MKergall:osmbonuspack:6.9.0")
    implementation("org.osmdroid:osmdroid-android:6.1.13")
    implementation("org.apache.commons:commons-lang3:3.8.1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:okhttp:4.7.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}