@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildkonfig)
}

buildkonfig {
  packageName = "com.example.community_app"

  val localProperties = Properties()
  val localPropertiesFile = project.rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
  }

  defaultConfigs {
    buildConfigField(
      FieldSpec.Type.STRING,
      "GEOAPIFY_KEY",
      localProperties.getProperty("GEOAPIFY_KEY") ?: ""
    )

    buildConfigField(
      FieldSpec.Type.STRING,
      "GEOAPIFY_URL",
      localProperties.getProperty("GEOAPIFY_URL") ?: ""
    )
  }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(libs.koin.core)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.work.runtime.ktx)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.play.services.location)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
            implementation(libs.compose.icons)
            implementation(libs.kotlinx.coroutines)

            implementation(libs.moko.permissions.compose)

            implementation(libs.compose.navigation)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.room.runtime)
            implementation("androidx.room:room-ktx:${libs.versions.room.get()}") {
              exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
            }
            implementation(libs.sqlite.bundled)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            api(libs.bundles.koin)
            implementation(libs.bundles.ktor.client)
            implementation(libs.bundles.coil)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    debugImplementation(compose.uiTooling)
}

android {
    namespace = "com.example.community_app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.community_app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}