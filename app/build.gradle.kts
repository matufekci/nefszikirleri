plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.nefszikir.kdhrmq"
    minSdk = 24
    targetSdk = 36
    versionCode = 3
    versionName = "2.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    val debugKeystoreFile = file("${rootDir}/debug.keystore")
    if (debugKeystoreFile.exists()) {
      create("debugConfig") {
        storeFile = debugKeystoreFile
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    } else {
      // Fallback: let AGP generate default debug keystore if custom one missing
      // This prevents build failure in CI / fresh clones without debug.keystore
      create("debugConfig") {
        // No explicit storeFile - AGP will use default debug keystore
        // We still set passwords to match default expectations
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }

    val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH") ?: System.getenv("KEYSTORE_PATH")
    val storePasswordEnv = System.getenv("RELEASE_STORE_PASSWORD") ?: System.getenv("STORE_PASSWORD")
    val keyAliasEnv = System.getenv("RELEASE_KEY_ALIAS") ?: System.getenv("KEY_ALIAS")
    val keyPasswordEnv = System.getenv("RELEASE_KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")

    val isReleaseSigningConfigured = !keystorePath.isNullOrBlank() &&
        file(keystorePath).exists() &&
        !storePasswordEnv.isNullOrBlank() &&
        !keyAliasEnv.isNullOrBlank() &&
        !keyPasswordEnv.isNullOrBlank()

    if (isReleaseSigningConfigured) {
      create("release") {
        storeFile = file(keystorePath!!)
        storePassword = storePasswordEnv
        keyAlias = keyAliasEnv
        keyPassword = keyPasswordEnv
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true

      val releaseSigning = signingConfigs.findByName("release")
      val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) } || 
                           gradle.startParameter.taskRequests.toString().contains("Release", ignoreCase = true)
                           
      if (isReleaseBuild && releaseSigning == null) {
          val msg = """
              |CONFIGURATION FAILED: Release signing configuration is missing.
              |Expected env vars: RELEASE_KEYSTORE_PATH, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD
              |Or: KEYSTORE_PATH, STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
              |For CI: ensure debug build works, release requires real keystore.
              |To bypass in CI for non-release tasks, don't run :assembleRelease.
          """.trimMargin()
          throw GradleException(msg)
      }

      signingConfig = releaseSigning

      proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
      )
    }
    debug {
      // Use custom debugConfig if available, otherwise default debug signing
      val debugCfg = signingConfigs.findByName("debugConfig")
      if (debugCfg != null && debugCfg.storeFile?.exists() == true) {
        signingConfig = debugCfg
      } else {
        // Let AGP use default debug keystore
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  
  sourceSets {
    getByName("androidTest").assets.srcDirs("$projectDir/schemas")
  }
  
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        it.maxHeapSize = "1g"
      }
    }
  }

  sourceSets {
    getByName("test") {
      assets.srcDirs(files("$projectDir/schemas"))
    }
  }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.auth)
  // App Check: Play Integrity for prod, Debug for dev, Recaptcha fallback
  implementation(libs.firebase.appcheck.playintegrity)
  implementation(libs.firebase.appcheck.debug)
  // implementation(libs.firebase.appcheck.recaptcha) // optional fallback
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.androidx.fragment.ktx)
  
  implementation(platform(libs.androidx.compose.bom))
  // implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  // implementation(libs.firebase.firestore)
  // implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  implementation(libs.androidx.work.runtime.ktx)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.room.testing)
  testImplementation("androidx.work:work-testing:2.10.0")
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}
