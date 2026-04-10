@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.humanReadable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(projects.serverDto)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertK)
}

application {
    mainClass = "nl.jacobras.codeobserver.cli.MainKt"
}

java {
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}
//tasks.named("startScripts") {
//    dependsOn("shadowJar")
//}
//tasks.named("jar") {
//    enabled = false
//}