package nl.jacobras.codeobserver.dashboard.modulegraph.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GraphVisualizerTest {

    @Test
    fun `empty graph`() {
        val graph = GraphVisualizer.build(
            modules = emptyMap(),
            groupingThreshold = 3
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
    fun `simple graph`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "moduleA" to listOf("moduleB", "moduleD"),
                "moduleB" to listOf("sub:c"),
                "sub:c" to emptyList()
            ),
            groupingThreshold = 3
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
    fun `simple graph with deprecated and forbidden configs`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "moduleA" to listOf("moduleB", "util"),
                "moduleB" to listOf("util"),
                "util" to emptyList()
            ),
            groupingThreshold = 3,
            config = listOf(
                GraphConfig.DeprecatedModule("util"),
                GraphConfig.ForbiddenDependency("moduleA", "moduleB")
            )
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                moduleA
                moduleB
                util
            
            %% Dependencies
                moduleA --> moduleB
                moduleA --> util
                moduleB --> util
            
            linkStyle 1 stroke:orange,stroke-width:3px;
            linkStyle 2 stroke:orange,stroke-width:3px;
            linkStyle 0 stroke:red,stroke-width:3px;
            """.trimIndent()
        )
    }

    @Test
    fun `simple graph with complicated forbidden config`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "app" to listOf("feature:a", "feature:b"),
                "feature:a" to emptyList(),
                "feature:b" to listOf("feature:a"), // Forbidden
                "component:a" to listOf("component:b"), // Forbidden
                "component:b" to listOf("feature:a") // Forbidden
            ),
            groupingThreshold = 3,
            config = listOf(
                GraphConfig.ForbiddenDependency("feature:*", "feature:*"),
                GraphConfig.ForbiddenDependency("component:*", "component:*"),
                GraphConfig.ForbiddenDependency("component:*", "feature:*"),
            )
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                app
                feature:a
                feature:b
                component:a
                component:b
            
            %% Dependencies
                app --> feature:a
                app --> feature:b
                feature:b --> feature:a
                component:a --> component:b
                component:b --> feature:a
            
            linkStyle 2 stroke:red,stroke-width:3px;
            linkStyle 3 stroke:red,stroke-width:3px;
            linkStyle 4 stroke:red,stroke-width:3px;
            """.trimIndent()
        )
    }

    @Test
    fun `simple graph with forbidden dependency in group`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "app" to listOf("feature:a"), // Forbidden
                "feature:a" to emptyList(),
                "feature:b" to emptyList(),
                "feature:c" to emptyList()
            ),
            groupingThreshold = 3,
            config = listOf(
                GraphConfig.ForbiddenDependency("app", "feature:*")
            )
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                app
                subgraph groupfeature ["feature"]
                    GROUPfeature["3 modules"]
                end
            
            %% Dependencies
                app --> groupfeature
            
            linkStyle 0 stroke:red,stroke-width:3px;
            """.trimIndent()
        )
    }

    @Test
    fun `graph with only one module starting at that module`() {
        val graph = GraphVisualizer.build(
            modules = mapOf("hello" to emptyList()),
            startModule = "hello",
            groupingThreshold = 3
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
    fun `graph with colors`() {
        val graph = GraphVisualizer.build(
            modules = mapOf("hello" to emptyList()),
            groupingThreshold = 3,
            moduleColors = mapOf("hello" to "#ff0000")
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                hello
            
            %% Dependencies
            
            classDef moduleType0 fill:#ff0000;
            class hello moduleType0
            """.trimIndent()
        )
    }

    @Test
    fun `too many modules`() {
        val graph = GraphVisualizer.build(
            modules = List(300) { "module$it" to emptyList<String>() }.toMap(),
            groupingThreshold = 3,
            nodeLimit = 30
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                A["Too large: 300 nodes (limit 30)."]
            """.trimIndent()
        )
    }

    @Test
    fun `too many groups`() {
        val graph = GraphVisualizer.build(
            modules = buildMap {
                put("module:a", emptyList())
                put("module:b", emptyList())
                put("module:c", emptyList())
                put("group:a", emptyList())
                put("group:b", emptyList())
                put("group:c", emptyList())
                put("large:a", emptyList())
                put("large:b", emptyList())
                put("large:c", emptyList())
            },
            groupingThreshold = 2,
            nodeLimit = 2
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                A["Too large: 3 nodes (limit 2)."]
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
            groupingThreshold = 3
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
            groupingThreshold = 3
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
            groupingThreshold = 3,
            nodeLimit = 3
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
            groupingThreshold = 3,
            nodeLimit = 3
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
            groupingThreshold = 3,
            nodeLimit = 3,
            startModule = "util"
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                util
                subgraph grouputil ["util"]
                    GROUPutil["4 modules"]
                end
            
            %% Dependencies
                util --> grouputil

            class util start
            classDef start fill:#a5a5b2;
            """.trimIndent()
        )
    }

    @Test
    fun `group module count only includes dependencies`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "app" to listOf("util:a", "util:b", "util:c"),
                "util:a" to emptyList(),
                "util:b" to emptyList(),
                "util:c" to emptyList(),
                "util:d" to emptyList(),
                "util:e" to emptyList()
            ),
            groupingThreshold = 3,
            nodeLimit = 3,
            startModule = "app"
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                app
                subgraph grouputil ["util"]
                    GROUPutil["3 modules"]
                end
            
            %% Dependencies
                app --> grouputil
            
            class app start
            classDef start fill:#a5a5b2;
            """.trimIndent()
        )
    }

    @Test
    fun `show only n layers deep`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "app" to listOf("feature:a", "feature:b"),
                "feature:a" to listOf("util:a"),
                "feature:b" to listOf("util:a"),
                "util:a" to emptyList()
            ),
            groupingThreshold = 3,
            nodeLimit = 10,
            startModule = "app",
            layerDepth = 1
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                app
                feature:a
                feature:b
            
            %% Dependencies
                app --> feature:a
                app --> feature:b
            
            class app start
            classDef start fill:#a5a5b2;
            """.trimIndent()
        )
    }

    @Test
    fun `startModule should never be grouped`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "module:a" to emptyList(),
                "module:b" to listOf(
                    "module:a",
                    "module:b",
                    "module:c"
                ),
                "module:c" to emptyList()
            ),
            groupingThreshold = 2,
            nodeLimit = 10,
            startModule = "module:b",
            layerDepth = 3
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                module:b
                subgraph groupmodule ["module"]
                    GROUPmodule["3 modules"]
                end
            
            %% Dependencies
                module:b --> groupmodule
            
            class module:b start
            classDef start fill:#a5a5b2;
            """.trimIndent()
        )
    }

    @Test
    fun `longest group takes preference when pointing dependencies to groups`() {
        val graph = GraphVisualizer.build(
            modules = mapOf(
                "framework:common:language" to listOf(
                    "framework:data:concurrency"
                ),
                "framework:common:util:kotlin" to emptyList(),
                "framework:common:util:time" to listOf(
                    "framework:common:language"
                ),
                "framework:data:concurrency" to emptyList(),
                "framework:ui:accessibility" to emptyList(),
                "util" to listOf(
                    "framework:common:util:time",
                    "framework:common:util:kotlin",
                    "framework:ui:accessibility",
                    "framework:data:concurrency"
                ),
            ),
            groupingThreshold = 2,
            nodeLimit = 10,
            startModule = "util",
            layerDepth = 3
        )

        assertThat(graph).isEqualTo(
            """
            graph TD
                util
                subgraph groupframework ["framework"]
                    GROUPframework["5 modules"]
                end
                subgraph groupframework:common:util ["framework:common:util"]
                    GROUPframework:common:util["2 modules"]
                end
            
            %% Dependencies
                groupframework:common:util --> groupframework
                util --> groupframework:common:util
                util --> groupframework
            
            class util start
            classDef start fill:#a5a5b2;
            """.trimIndent()
        )
    }
}