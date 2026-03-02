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

    @Test
    fun `parse accessor mapping`() {
        val modules = listOf(
            "a:sub",
            "b:deeper:sub",
            "component:with_underscore",
            "component:with-hyphen",
            "root-hyphen:a-module",
            "root-hyphen:b"
        )

        val accessorMapping = GradleSettingsParser.parseAccessorMapping(modules)
        assertThat(accessorMapping).isEqualTo(
            mapOf(
                "a.sub" to "a:sub",
                "b.deeper.sub" to "b:deeper:sub",
                "component.withUnderscore" to "component:with-underscore",
                "component.withHyphen" to "component:with-hyphen",
                "rootHyphen.aModule" to "root-hyphen:a-module",
                "rootHyphen.b" to "root-hyphen:b"
            )
        )
    }
}