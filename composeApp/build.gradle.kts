import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.get()
    id("org.jetbrains.kotlin.native.cocoapods")
    id("app.cash.sqldelight") version "2.1.0"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += listOf("-g") // ✅ Enable debug symbol generation
        }
    }

    cocoapods {
        summary = "FloveItControl iOS Framework"
        homepage = "https://floveit.com"
        version = "1.0"
        ios.deploymentTarget = "15.0"
        podfile = project.file("../iosApp/Podfile")      // 👈 👈 👈 MISSING in your file!
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }

    }



    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.material.icons.extended)
            implementation (libs.barcode.scanning)

            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.network)


        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.multiplatform.settings)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.voyager.navigator)

            implementation(libs.kotlinx.datetime) // or latest

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.network)



        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

    }
}

android {
    namespace = "com.floveit.floveitcontrol"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.floveit.floveitcontrol"
        minSdk = libs.versions.android.minSdk.get().toInt()
        //noinspection OldTargetApi
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

dependencies {
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    debugImplementation(compose.uiTooling)
}

