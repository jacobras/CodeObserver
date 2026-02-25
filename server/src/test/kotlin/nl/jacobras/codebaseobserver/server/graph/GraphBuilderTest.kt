package nl.jacobras.codebaseobserver.server.graph

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GraphBuilderTest {

    @Test
    fun `empty graph`() {
        val graph = GraphBuilder.build(
            modules = emptyMap()
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
            
            %% Dependencies
        """.trimIndent()
        )
    }

    @Test
    fun `simple graph`() {
        val graph = GraphBuilder.build(
            modules = mapOf(
                "moduleA" to listOf("moduleB", "moduleC"),
                "moduleB" to listOf("moduleC"),
                "moduleC" to emptyList()
            )
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                moduleA
                moduleB
                moduleC
            
            %% Dependencies
                moduleA --> moduleB
                moduleA --> moduleC
                moduleB --> moduleC
        """.trimIndent()
        )
    }

    @Test
    fun `graph starts at starting point`() {
        val graph = GraphBuilder.build(
            modules = mapOf(
                "moduleA" to listOf("moduleB", "moduleC"),
                "moduleB" to listOf("moduleC"),
                "moduleC" to emptyList()
            ),
            startModule = "moduleB"
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                moduleB
                moduleC
            
            %% Dependencies
                moduleB --> moduleC

            class moduleB start
            classDef start fill:#a5a5b2;
        """.trimIndent()
        )
    }

    @Test
    fun `groups above threshold are grouped`() {
        val graph = GraphBuilder.build(
            modules = mapOf(
                "util" to listOf("util:a", "util:b", "util:c", "util:d", "util:e")
            ),
            groupThreshold = 3
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                util["util<br>(6 modules)"]
            
            %% Dependencies
        """.trimIndent()
        )
    }
}