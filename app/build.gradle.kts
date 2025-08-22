plugins {
    id("com.android.application")
}

android {
    namespace = "com.error.dhlvisitornotification"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.error.dhlvisitornotification"
        minSdk = 21  // Android 5.0 (API level 21) - Supports 99%+ devices
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "EMAIL_SENDER", "\"user1@localhost\"")
        buildConfigField("String", "EMAIL_APP_PASSWORD", "\"password123\"")
        buildConfigField("String", "OWNER_EMAIL", "\"user2@localhost\"")

        // Enable vector drawables for older devices
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/NOTICE",
                "META-INF/mimetypes.default",
                "META-INF/mailcap.default",
                "META-INF/javamail.providers",
                "META-INF/javamail.default.providers"
            )
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

configurations.all {
    exclude(group = "org.apache.httpcomponents")
}

dependencies {
    // Use compatible versions for older Android
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core:1.12.0")

    // QR Code scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Email dependencies
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("com.sun.activation:jakarta.activation:2.0.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}