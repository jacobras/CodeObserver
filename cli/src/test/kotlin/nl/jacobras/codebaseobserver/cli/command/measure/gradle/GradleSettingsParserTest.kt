package nl.jacobras.codebaseobserver.cli.command.measure.gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GradleSettingsParserTest {

    @Test
    fun parse() {
        val file = """include(
            ":module1",
            ":module2", // Someone's comment
            ":module3", // Someone's comment
            ":module4:sub:a",
            ":module4:sub:b"
        )
        include(":module5")
        include(":feature:multi-column-layout")
        """.trimIndent()

        val modules = GradleSettingsParser.parseModules(file)

        assertThat(modules).isEqualTo(
            listOf(
                "module1",
                "module2",
                "module3",
                "module4:sub:a",
                "module4:sub:b",
                "module5",
                "feature:multi-column-layout"
            )
        )
    }
}