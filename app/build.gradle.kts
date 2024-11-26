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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    // https://mvnrepository.com/artifact/org.osmdroid/osmdroid-shape
    implementation("org.osmdroid:osmdroid-shape:6.1.20")
    implementation("org.mapsforge:mapsforge-map-android:0.21.0")
    implementation("org.mapsforge:mapsforge-map-reader:0.21.0")

    implementation("com.caverock:androidsvg:1.4")
    implementation(libs.play.services.maps)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}