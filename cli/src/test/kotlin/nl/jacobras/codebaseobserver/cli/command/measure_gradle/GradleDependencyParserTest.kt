package nl.jacobras.codebaseobserver.cli.command.measure_gradle

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
                implementation(projects.d.e.f)
                implementation(":g:h")
            }
        """.trimIndent()

        val dependencies = GradleDependencyParser.parse(file)

        assertThat(dependencies).isEqualTo(listOf("a:b", "c", "d:e:f", "g:h"))
    }
}