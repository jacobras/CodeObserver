plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.codeobserver.detekt) apply true
}

subprojects {
    group = "nl.jacobras.codeobserver"
    version = "0.4.0"
}