package nl.jacobras.codebaseobserver.cli.command.measure.code

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ImportCounterTest {

    @Test
    fun `empty text`() {
        val counts = ImportCounter.count("", listOf("com.example.Foo"))
        assertThat(counts).isEqualTo(mapOf("com.example.Foo" to 0))
    }

    @Test
    fun `exact import match`() {
        val text = """
            package com.example

            import com.example.Foo

            class Bar
        """.trimIndent()
        val counts = ImportCounter.count(text, listOf("com.example.Foo"))
        assertThat(counts).isEqualTo(mapOf("com.example.Foo" to 1))
    }

    @Test
    fun `aliased import match`() {
        val text = "import com.example.Foo as Bar"
        val counts = ImportCounter.count(text, listOf("com.example.Foo"))
        assertThat(counts).isEqualTo(mapOf("com.example.Foo" to 1))
    }

    @Test
    fun `no match for different import`() {
        val text = "import com.example.Other"
        val counts = ImportCounter.count(text, listOf("com.example.Foo"))
        assertThat(counts).isEqualTo(mapOf("com.example.Foo" to 0))
    }

    @Test
    fun `no match for prefix of rule`() {
        val text = "import com.example.FooBar"
        val counts = ImportCounter.count(text, listOf("com.example.Foo"))
        assertThat(counts).isEqualTo(mapOf("com.example.Foo" to 0))
    }

    @Test
    fun `counts multiple occurrences across lines`() {
        val text = """
            import com.example.Foo
            import com.example.Foo as Baz
        """.trimIndent()
        val counts = ImportCounter.count(text, listOf("com.example.Foo"))
        assertThat(counts).isEqualTo(mapOf("com.example.Foo" to 2))
    }

    @Test
    fun `counts multiple rules independently`() {
        val text = """
            import com.example.Foo
            import com.example.Bar
            import com.example.Foo as Baz
        """.trimIndent()
        val counts = ImportCounter.count(
            text = text,
            rules = listOf(
                "com.example.Foo",
                "com.example.Bar"
            )
        )
        assertThat(counts).isEqualTo(
            mapOf(
                "com.example.Foo" to 2,
                "com.example.Bar" to 1
            )
        )
    }

    @Test
    fun `ignores indented import`() {
        val text = "    import com.example.Foo"
        val counts = ImportCounter.count(text, listOf("com.example.Foo"))
        assertThat(counts).isEqualTo(mapOf("com.example.Foo" to 1))
    }

    @Test
    fun `extractImportPath returns null for non-import line`() {
        assertThat(ImportCounter.extractImportPath("class Foo")).isNull()
        assertThat(ImportCounter.extractImportPath("")).isNull()
        assertThat(ImportCounter.extractImportPath("// import com.example.Foo")).isNull()
    }

    @Test
    fun `extractImportPath returns path for plain import`() {
        assertThat(ImportCounter.extractImportPath("import com.example.Foo"))
            .isEqualTo("com.example.Foo")
    }

    @Test
    fun `extractImportPath returns path for aliased import`() {
        assertThat(ImportCounter.extractImportPath("import com.example.Foo as Bar"))
            .isEqualTo("com.example.Foo")
    }
}