package nl.jacobras.codebaseobserver.cli.command.measure_gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import nl.jacobras.codebaseobserver.cli.command.measure.gradle.GradleSettingsParser
import kotlin.test.Test

class GradleSettingsParserTest {

    @Test
    fun parse() {
        val file = """include(
            ":module1",
            ":module2", // Someone's comment
            ":module3", // Someone's comment
            ":module4:a",
            ":module4:b"
        )"""

        val modules = GradleSettingsParser.parseModules(file)

        assertThat(modules).isEqualTo(listOf("module1", "module2", "module3", "module4:a", "module4:b"))
    }
}