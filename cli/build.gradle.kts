@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.kotlinx.json)
}

application {
    mainClass = "nl.jacobras.codebaseobserver.cli.MainKt"
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}