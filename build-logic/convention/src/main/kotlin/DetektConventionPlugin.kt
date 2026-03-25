import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(
                libs.findLibrary("detekt-gradlePlugin").get().get().group
            )

            tasks.register<Detekt>("detektMultiModule") {
                description = "Detekt scan for all modules in the project"
                parallel = true
                ignoreFailures = false
                buildUponDefaultConfig = true
                config.setFrom(files("$rootDir/detekt.yml"))
                setSource(file(projectDir))
                include("**/*.kt", "**/*.kts")
                exclude("**/build/**")
                reports {
                    html.required.set(true)
                }
                dependencies {
                    "detektPlugins"(libs.library("detekt-compose"))
                    "detektPlugins"(libs.library("detekt-formatting"))
                }
            }
        }
    }
}