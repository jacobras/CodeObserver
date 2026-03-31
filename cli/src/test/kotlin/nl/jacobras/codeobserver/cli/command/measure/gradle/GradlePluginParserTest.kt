package nl.jacobras.codeobserver.cli.command.measure.gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GradlePluginParserTest {

    @Test
    fun `empty file`() {
        val dependencies = GradlePluginParser.parse(text = "")
        assertThat(dependencies).isEqualTo(emptyList())
    }

    @Test
    fun `regular plugins`() {
        val file = """
            plugins {
                id("com.example.plugin")
                alias(libs.plugins.otherPlugin)
            }
        """.trimIndent()

        val dependencies = GradlePluginParser.parse(text = file)

        assertThat(dependencies).isEqualTo(
            listOf(
                "libs.plugins.otherPlugin",
                "com.example.plugin"
            )
        )
    }

    @Test
    fun `kotlin plugin`() {
        val file = """
            plugins {
                kotlin("jvm")
            }
        """.trimIndent()

        val dependencies = GradlePluginParser.parse(text = file)

        assertThat(dependencies).isEqualTo(
            listOf(
                "kotlin(\"jvm\")"
            )
        )
    }
}