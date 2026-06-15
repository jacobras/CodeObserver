import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.buildconfig)
    application
}

application {
    mainClass.set("nl.jacobras.codeobserver.server.ApplicationKt")
}

java {
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.bcrypt)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.migration.core)
    implementation(libs.exposed.migration.jdbc)
    implementation(libs.jgrapht.core)
    implementation(libs.kermit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.logback.classic)
    implementation(libs.semver)
    implementation(libs.sqlite.jdbc)

    implementation(projects.serverDto)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertK)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.server.test.host)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}

buildConfig {
    val isReleaseBuild = providers.gradleProperty("release")
        .map { it.toBoolean() }
        .getOrElse(false)
    buildConfigField("RELEASE", isReleaseBuild)
}