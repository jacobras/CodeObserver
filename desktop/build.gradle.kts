import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.buildconfig)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.clikt)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.multiplatformPaths)

            implementation(projects.cli)
            implementation(projects.server)
            implementation(projects.serverDto)
            implementation(projects.util.design)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "nl.jacobras.codeobserver.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            modules("java.sql", "java.naming")
            appResourcesRootDir.set(project.layout.projectDirectory.dir("app"))
            packageName = "nl.jacobras.codeobserver"
            packageVersion = "1.0.0"

            windows {
                menu = true
            }
            macOS {
                dockName = "CodeObserver"
            }
        }
    }
}

buildConfig {
    buildConfigField("VERSION", version.toString())
}

val copyWebDist by tasks.registering(Copy::class) {
    from(project(":web").layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
    into(layout.projectDirectory.dir("app/common/web"))
    dependsOn(":web:wasmJsBrowserDistribution")
}

tasks.matching { it.name == "prepareAppResources" || it.name == "hotRunJvm" }
    .configureEach { dependsOn(copyWebDist) }