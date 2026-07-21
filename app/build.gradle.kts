plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.scoutingsampdoria.persone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.scoutingsampdoria.persone"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        // versionName di default; sovrascritto dal workflow con -PversionNameOverride=N
        versionName = (project.findProperty("versionNameOverride") as String?)?.let { "1.0.$it" } ?: "1.0-dev"

        // Cambia questo con il tuo dominio PythonAnywhere reale.
        buildConfigField("String", "BASE_URL", "\"https://scoutingsampdoria.pythonanywhere.com/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // Compose BOM: allinea le versioni di tutte le librerie Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigazione tra schermate
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel per Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Rete: Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Salvataggio sicuro del token JWT
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
