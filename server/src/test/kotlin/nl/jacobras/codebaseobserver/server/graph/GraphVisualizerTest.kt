package nl.jacobras.codebaseobserver.server.graph

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GraphVisualizerTest {

    @Test
    fun `empty graph`() {
        val graph = GraphVisualizer.build(
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
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "moduleA" to listOf("moduleB", "moduleD"),
                "moduleB" to listOf("sub:c"),
                "sub:c" to emptyList()
            )
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                moduleA
                moduleB
                sub:c
            
            %% Dependencies
                moduleA --> moduleB
                moduleA --> moduleD
                moduleB --> sub:c
        """.trimIndent()
        )
    }

    @Test
    fun `very wide graph`() {
        val graph = GraphVisualizer.build(
            modules = List(300) { "module$it" to emptyList<String>() }.toMap()
        )

        assertThat(graph).isEqualTo("Too large: 300 modules.")
    }

    @Test
    fun `graph starts at starting point`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "moduleA" to listOf("moduleB", "sub:c"),
                "moduleB" to listOf("sub:c"),
                "sub:c" to emptyList()
            ),
            startModule = "moduleB"
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                moduleB
                sub:c
            
            %% Dependencies
                moduleB --> sub:c

            class moduleB start
            classDef start fill:#a5a5b2;
        """.trimIndent()
        )
    }

    @Test
    fun `graph starts at sub module`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "app" to listOf("feature:products", "util:design"),
                "feature:products" to listOf("util:design"),
                "feature:welcome" to listOf("util:design"),
                "util:design" to emptyList()
            ),
            startModule = "feature:products"
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                feature:products
                util:design
            
            %% Dependencies
                feature:products --> util:design
            
            class feature:products start
            classDef start fill:#a5a5b2;
        """.trimIndent()
        )
    }

    @Test
    fun `groups above threshold are grouped`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "util:a" to emptyList(),
                "util:b" to emptyList(),
                "util:c" to emptyList(),
                "util:d" to emptyList(),
                "util:e" to emptyList()
            ),
            groupThreshold = 3
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                subgraph grouputil ["util"]
                    GROUPutil["5 modules"]
                end
            
            %% Dependencies
        """.trimIndent()
        )
    }

    @Test
    fun `nested groups`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "util:a" to emptyList(),
                "util:b" to emptyList(),
                "util:c" to emptyList(),
                "util:d" to emptyList(),
                "util:sub" to listOf(
                    "util:sub:1",
                    "util:sub:2",
                    "util:sub:3"
                )
            ),
            groupThreshold = 3
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                subgraph grouputil ["util"]
                    GROUPutil["8 modules"]
                end
            
            %% Dependencies
        """.trimIndent()
        )
    }
}