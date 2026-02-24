package nl.jacobras.codebaseobserver.cli.command.measure.gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GradleDependencyParserTest {

    @Test
    fun parse() {
        val file = """
            dependencies {
                implementation(projects.a.b)
                implementation(projects.c)
                implementation(projects.util.design)
                implementation(project(":module:sub"))
                implementation(project(":util:debug-settings"))
            }
        """.trimIndent()

        val dependencies = GradleDependencyParser.parse(file)

        assertThat(dependencies).isEqualTo(
            listOf(
                "a:b",
                "c",
                "util:design",
                "module:sub",
                "util:debug-settings"
            )
        )
    }
}