import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.buildconfig)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.androidx.nav)
            implementation(libs.carbon)
            implementation(libs.compose.viewmodel)
            implementation(libs.coroutines)
            implementation(libs.humanReadable)
            implementation(libs.kermit)
            implementation(libs.kotlin.result)
            implementation(libs.kotlin.result.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.js)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.semver)
            implementation(libs.vico)

            implementation(projects.serverDto)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.assertK)
        }
    }
}

buildConfig {
    buildConfigField("VERSION", version.toString())

    val isDemo = providers.gradleProperty("demo")
        .map { it.toBoolean() }
        .getOrElse(false)
    buildConfigField("IS_DEMO", isDemo)
}