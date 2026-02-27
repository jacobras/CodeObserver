package nl.jacobras.codebaseobserver.server.graph

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GraphVisualizerTest {

    @Test
    fun `empty graph`() {
        val graph = GraphVisualizer.build(
            modules = emptyMap(),
            groupThreshold = 3
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                A[No data to display]
            
            %% Dependencies
        """.trimIndent()
        )
    }

    @Test
    fun `graph with only one module starting at that module`() {
        val graph = GraphVisualizer.build(
            modules = mapOf("hello" to emptyList()),
            startModule = "hello",
            groupThreshold = 3
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                hello
            
            %% Dependencies
            
            class hello start
            classDef start fill:#a5a5b2;
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
            ),
            groupThreshold = 3
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
            modules = List(300) { "module$it" to emptyList<String>() }.toMap(),
            groupThreshold = 3
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                A[Too large: 300 modules.]
        """.trimIndent()
        )
    }

    @Test
    fun `graph starts at starting point`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "moduleA" to listOf("moduleB", "sub:c"),
                "moduleB" to listOf("sub:c"),
                "sub:c" to emptyList()
            ),
            startModule = "moduleB",
            groupThreshold = 3
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
            startModule = "feature:products",
            groupThreshold = 3
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
                    "util:sub:3",
                    "randomExcludedFromCount"
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
                grouputil --> randomExcludedFromCount
        """.trimIndent()
        )
    }

    @Test
    fun `irrelevant group is ignored when starting at util`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "util" to listOf("util:a", "util:b", "util:c"),
                "util:a" to emptyList(),
                "util:b" to emptyList(),
                "util:c" to emptyList(),
                "something-else:a" to emptyList(),
                "something-else:b" to emptyList(),
                "something-else:c" to emptyList()
            ),
            groupThreshold = 3,
            startModule = "util"
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                subgraph grouputil ["util"]
                    GROUPutil["4 modules"]
                end
            
            %% Dependencies

            class util start
            classDef start fill:#a5a5b2;
        """.trimIndent()
        )
    }
}