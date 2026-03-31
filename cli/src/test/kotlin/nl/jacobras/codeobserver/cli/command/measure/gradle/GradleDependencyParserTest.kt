package nl.jacobras.codeobserver.cli.command.measure.gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GradleDependencyParserTest {

    @Test
    fun `empty file`() {
        val dependencies = GradleDependencyParser.parse(
            text = "",
            accessorMapping = emptyMap()
        )
        assertThat(dependencies).isEqualTo(emptyList())
    }

    @Test
    fun `regular accessor`() {
        val file = """
            dependencies {
                implementation(project(":a"))
                implementation(project(":b:sub"))
                implementation(project(":c:sub-with-dashes"))
            }
        """.trimIndent()

        val dependencies = GradleDependencyParser.parse(
            text = file,
            accessorMapping = emptyMap()
        )

        assertThat(dependencies).isEqualTo(
            listOf(
                "a",
                "b:sub",
                "c:sub-with-dashes"
            )
        )
    }

    @Test
    fun `typesafe accessor`() {
        val file = """
            dependencies {
                implementation( projects.a )
                implementation(projects.b.sub)
                implementation(projects.c.subWithDashes)
            }
        """.trimIndent()

        val dependencies = GradleDependencyParser.parse(
            text = file,
            accessorMapping = mapOf(
                "c.subWithDashes" to "c:sub-with-dashes"
            )
        )

        assertThat(dependencies).isEqualTo(
            listOf(
                "a",
                "b:sub",
                "c:sub-with-dashes"
            )
        )
    }

    @Test
    fun `ignore test dependencies`() {
        val file = """
            dependencies {
                implementation(projects.a)
                testImplementation(projects.b.sub)
                androidTestImplementation(projects.c.subWithDashes)
            }
        """.trimIndent()

        val dependencies = GradleDependencyParser.parse(
            text = file,
            accessorMapping = emptyMap()
        )

        assertThat(dependencies).isEqualTo(
            listOf(
                "a"
            )
        )
    }
}