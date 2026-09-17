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
  // compileSdk 37: androidx.core 1.19.0 / core-ktx 1.19.0 AAR metadata'si
  // "compile against version 37 or later" ve "AGP 9.1.0 or higher" istiyor
  // (AGP'miz 9.1.1). targetSdk BILEREK 36'da: targetSdk uygulamayi yeni
  // calisma zamani davranislarina sokar, compileSdk yalnizca derleme
  // yuzeyini genisletir. minSdk 24 de degismedi.
  compileSdk { version = release(37) }

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
      // Keystore tipini dosyanin magic baytlarindan tespit et.
      // JKS dosyalari FEEDFEED ile, PKCS12 (DER) dosyalari 3082 ile baslar.
      // Yanlis storeType verilirse imzalama adimi "keystore was tampered with /
      // not found" gibi yaniltici hatalarla patlar. Repodaki debug.keystore PKCS12.
      val magic = debugKeystoreFile.inputStream().use { it.readNBytes(4) }
      val isJksKeystore = magic.size >= 4 &&
        magic[0] == 0xFE.toByte() && magic[1] == 0xED.toByte() &&
        magic[2] == 0xFE.toByte() && magic[3] == 0xED.toByte()
      create("debugConfig") {
        storeFile = debugKeystoreFile
        storeType = if (isJksKeystore) "jks" else "PKCS12"
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

      // FAIL-FAST: release build ASLA debug keystore ile imzalanamaz.
      // Gecmiste CI, debug.keystore'i RELEASE_KEYSTORE_PATH olarak besleyip
      // release'i debug anahtariyla imzaliyordu. Bunu tamamen engelliyoruz:
      // gercek upload keystore CI secret'indan gelmelidir.
      val debugKeystorePath = file("${rootDir}/debug.keystore").absolutePath
      val releaseStorePath = releaseSigning?.storeFile?.absolutePath
      if (releaseSigning != null &&
          (releaseStorePath == debugKeystorePath || releaseSigning.keyAlias == "androiddebugkey")) {
          throw GradleException(
              "RELEASE IMZA REDDEDILDI: debug keystore release imzasi olarak KULLANILAMAZ. " +
              "Gercek upload keystore'unu CI secret olarak saglayin " +
              "(RELEASE_KEYSTORE_BASE64 + RELEASE_STORE_PASSWORD + RELEASE_KEY_ALIAS + RELEASE_KEY_PASSWORD). " +
              "Play App Signing key Google'da, upload key CI'da yonetilir."
          )
      }

      // Gercek keystore yoksa release UNSIGNED uretilir: assembleRelease/bundleRelease
      // yine calisir ve artifact uretir; imza yalnizca gercek keystore saglaninca uygulanir.
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
  lint {
    // CI'daki "Report lint results" adimi XML raporu okuyup sayilari
    // annotation'a ceviriyor; rapor uretilmezse job kirmizi oluyor. Boylece
    // lint'in hic calismamasi sessizce yesil gorunmuyor.
    xmlReport = true
    htmlReport = true
    // Lint bulgulari icin politika CI'da (report-lint.sh yalnizca FATAL'de
    // kirmiziya cevirir); derleme burada kesilmiyor.
    abortOnError = false
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
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.moshi.kotlin)
  implementation(libs.androidx.work.runtime.ktx)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.room.testing)
  testImplementation(libs.androidx.work.testing)
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
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}
