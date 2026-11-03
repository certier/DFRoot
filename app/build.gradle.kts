import java.util.Properties

plugins {
    id("com.android.application")
}

val signingProps = Properties()
val signingPropsFile = rootProject.file("signing.properties")
if (signingPropsFile.exists()) signingProps.load(signingPropsFile.inputStream())

// Pull-request debug builds do not receive release secrets. Only create the
// release signing config when every required property is present.
val releaseSigningConfigured = listOf(
    "KEYSTORE_FILE",
    "KEYSTORE_PASSWORD",
    "KEY_ALIAS",
    "KEY_PASSWORD"
).all { !signingProps.getProperty(it).isNullOrBlank() }

android {
    namespace = "df.root"
    compileSdk = 37

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "df.root"
        minSdk = 31
        targetSdk = 37
        versionCode = 408
        versionName = "4.8-redmi9t-dev"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(requireNotNull(signingProps.getProperty("KEYSTORE_FILE")))
                storePassword = requireNotNull(signingProps.getProperty("KEYSTORE_PASSWORD"))
                keyAlias = requireNotNull(signingProps.getProperty("KEY_ALIAS"))
                keyPassword = requireNotNull(signingProps.getProperty("KEY_PASSWORD"))
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        checkReleaseBuilds = false
    }

    externalNativeBuild {
        cmake {
            path("src/main/jni/CMakeLists.txt")
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set(output.versionName.map { vn -> "DFRoot_${vn}.apk" })
        }
    }
}

dependencies {
}
