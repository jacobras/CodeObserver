package nl.jacobras.codeobserver.cli.command.measure.code

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
    fun `counts indented import`() {
        val text = "    import com.example.Foo"
        val counts = ImportCounter.count(text, listOf("com.example.Foo"))
        assertThat(counts).isEqualTo(mapOf("com.example.Foo" to 1))
    }

    @Test
    fun `wildcard rule matches import under that package`() {
        val text = "import androidx.compose.material3.Card"
        val counts = ImportCounter.count(text, listOf("androidx.compose.material3.*"))
        assertThat(counts).isEqualTo(mapOf("androidx.compose.material3.*" to 1))
    }

    @Test
    fun `wildcard rule matches multiple imports under that package`() {
        val text = """
            import androidx.compose.material3.Card
            import androidx.compose.material3.Button
        """.trimIndent()
        val counts = ImportCounter.count(text, listOf("androidx.compose.material3.*"))
        assertThat(counts).isEqualTo(mapOf("androidx.compose.material3.*" to 2))
    }

    @Test
    fun `wildcard rule does not match import from sibling package`() {
        val text = "import androidx.compose.material3extra.Card"
        val counts = ImportCounter.count(text, listOf("androidx.compose.material3.*"))
        assertThat(counts).isEqualTo(mapOf("androidx.compose.material3.*" to 0))
    }

    @Test
    fun `wildcard rule does not match parent package`() {
        val text = "import androidx.compose.material3"
        val counts = ImportCounter.count(text, listOf("androidx.compose.material3.*"))
        assertThat(counts).isEqualTo(mapOf("androidx.compose.material3.*" to 0))
    }

    @Test
    fun `wildcard rule matches code reference`() {
        val text = "@ColorRes backgroundColorId: Int = R.color.snackbar_background_color"
        val counts = ImportCounter.count(text, listOf("R.color.*"))
        assertThat(counts).isEqualTo(mapOf("R.color.*" to 1))
    }

    @Test
    fun `wildcard rule counts multiple code references on same line`() {
        val text = "return if (x) R.color.primary else R.color.secondary"
        val counts = ImportCounter.count(text, listOf("R.color.*"))
        assertThat(counts).isEqualTo(mapOf("R.color.*" to 2))
    }

    @Test
    fun `wildcard rule is case sensitive for code references`() {
        val text = "val x = R.Color.primary"
        val counts = ImportCounter.count(text, listOf("R.color.*"))
        assertThat(counts).isEqualTo(mapOf("R.color.*" to 0))
    }

    @Test
    fun `wildcard rule does not match code reference without trailing identifier`() {
        val text = "val x = R.color."
        val counts = ImportCounter.count(text, listOf("R.color.*"))
        assertThat(counts).isEqualTo(mapOf("R.color.*" to 0))
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