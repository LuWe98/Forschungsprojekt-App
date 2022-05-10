plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")

    val kotlinVersion = "1.6.0"
    kotlin("plugin.serialization") version kotlinVersion
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.serverless.forschungsprojectfaas"
        minSdk = 23
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    //Base
    val jetBrainsAnnotationVersion: String by project
    implementation("org.jetbrains:annotations:$jetBrainsAnnotationVersion")

    val appCompatVersion: String by project
    implementation("androidx.appcompat:appcompat:$appCompatVersion")

    val androidXCoreVersion: String by project
    implementation("androidx.core:core-ktx:$androidXCoreVersion")


    //Material Design
    val materialDesignVersion: String by project
    implementation("com.google.android.material:material:$materialDesignVersion")


    //Constraint- and MotionLayout
    val constraintLayoutVersion: String by project
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")


    //Fragments
    val fragmentVersion: String by project
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")


    //Livecycle
    val liveCycleVersion: String by project
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$liveCycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$liveCycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$liveCycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$liveCycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$liveCycleVersion")

    val liveCycleVersionOlder: String by project
    implementation("androidx.lifecycle:lifecycle-extensions:$liveCycleVersionOlder")


    //Navigation Component
    val navigationVersion: String by project
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navigationVersion")


    //Hilt
    val hiltVersion: String by project
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.camera:camera-view:1.1.0-beta03")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    val hiltViewModelVersion: String by project
//    implementation("androidx.hilt:hilt-lifecycle-viewmodel:$hiltViewModelVersion")
    val hiltAndroidXVersion: String by project
    kapt("androidx.hilt:hilt-compiler:$hiltAndroidXVersion")
    val hiltFragmentVersion: String by project
    implementation("androidx.hilt:hilt-navigation-fragment:$hiltFragmentVersion")


    //Coroutines
    val coroutineVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")


    //Ktor
    val ktorVersion: String by project
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")


    //Room Database
    val roomVersion: String by project
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    //Kotlin Serialisation
    val kotlinSerialisationVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerialisationVersion")

    //Preference-Datastore
    val preferenceDatastoreVersion: String by project
    implementation("androidx.datastore:datastore-preferences:$preferenceDatastoreVersion")


    //Splash Screens
    val splashScreenVersion: String by project
    implementation("androidx.core:core-splashscreen:$splashScreenVersion")


    
    //TODO -> Braucht man nicht mehr!
    val cameraxVersion: String by project
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")


    //GLIDE for Image display
    implementation("com.github.bumptech.glide:glide:4.13.0")
    kapt("com.github.bumptech.glide:compiler:4.13.0")


    implementation("com.davemorrissey.labs:subsampling-scale-image-view-androidx:3.10.0")



    //EIGENE LIBRARY
    implementation("com.github.LucaWeinmann:AndroidCoroutineFlowUtils:1.0.5")


    //Testing
    val jUnitVersion: String by project
    testImplementation("junit:junit:$jUnitVersion")

    val androidJUnitVersion: String by project
    androidTestImplementation("androidx.test.ext:junit:$androidJUnitVersion")

    val espressoVersion: String by project
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")

}