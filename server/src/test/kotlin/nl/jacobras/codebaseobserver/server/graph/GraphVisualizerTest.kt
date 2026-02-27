package nl.jacobras.codebaseobserver.server.graph

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
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
    fun `notes`() {
        val graph = GraphVisualizer.build(notesDependencies)
        println(graph)
    }

    @Test
    fun `notesWithStart`() {
        val graph = GraphVisualizer.build(
            modules = notesDependencies,
            startModule = "app",
            groupThreshold = 3
        )
        println(graph)
    }

//    @Test
//    fun `do not merge children into real existing root module`() {
//        val graph = GraphBuilder.build(
//            modules = mapOf(
//                "util" to emptyList(),
//                "util:a" to emptyList(),
//                "util:b" to emptyList(),
//                "util:c" to emptyList(),
//                "util:d" to emptyList()
//            ),
//            groupThreshold = 3
//        )
//
//        assertThat(graph).isEqualTo(
//            """
//            graph TD
//                util
//                util:a
//                util:b
//                util:c
//                util:d
//
//            %% Dependencies
//        """.trimIndent()
//        )
//    }

//    @Test
//    fun `do not merge groups that into modules that already exist`() {
//        val graph = GraphBuilder.build(
//            modules = mapOf(
//                "analytics" to listOf("app:a"),
//                "app" to listOf(
//                    "analytics",
//                    "app:a",
//                    "app:b",
//                    "app:c",
//                    "app:d"
//                ),
//                "app:a" to emptyList(),
//                "app:b" to emptyList(),
//                "app:c" to emptyList(),
//                "app:d" to emptyList()
//            ),
//            groupThreshold = 3
//        )
//
//        assertThat(graph).isEqualTo(
//            """
//            graph TD
//                analytics
//                app
//                app:a
//                app:b
//                app:c
//                app:d
//
//            %% Dependencies
//                analytics --> app:a
//        """.trimIndent()
//        )
//    }
}

val notesDependencies: Map<String, List<String>> = mapOf(

    "app" to listOf(
        "component:encryption",
        "component:serialization",
        "feature:encryption",
        "feature:multi-column-layout",
        "i18n",
        "util",
        "util:design",
        "util:preferences",
        "util:random",
        "util:yaml",
        "feature:debug-settings",
        "domain-test",
        "component:cloudservice",
        "component:notebooks",
        "component:pictures",
        "component:purchases",
        "component:security",
        "component:settings",
        "component:sync",
        "component:texteditor",
        "composed:richcontent",
        "database-legacy",
        "domain",
        "feature:backups",
        "feature:cloudaccounts",
        "feature:cloudservice",
        "feature:compare-notes",
        "feature:docs",
        "feature:editor",
        "feature:logbook-viewer",
        "feature:notebooks",
        "feature:notes",
        "feature:onboarding",
        "feature:pictures",
        "feature:security",
        "feature:settings",
        "feature:sync"
    ),

    "component:cloudservice" to listOf(
        "util:text",
        "util"
    ),

    "component:encryption" to listOf(
        "domain",
        "i18n",
        "util:design",
        "util:encryption",
        "database",
        "util"
    ),

    "component:formatting-bar" to listOf(
        "i18n",
        "util:design",
        "util"
    ),

    "component:formatting-popup" to listOf(
        "component:formatting-bar",
        "i18n",
        "util:design",
        "util"
    ),

    "component:notebooks" to listOf(
        "database",
        "util"
    ),

    "component:pictures" to listOf(
        "database",
        "domain",
        "util"
    ),

    "component:purchases" to listOf(
        "util"
    ),

    "component:security" to listOf(
        "domain",
        "util"
    ),

    "component:serialization" to listOf(
        "domain",
        "util:async",
        "util:date",
        "util:encryption",
        "util:kmp",
        "util:yaml"
    ),

    "component:settings" to listOf(
        "util"
    ),

    "component:sync" to listOf(
        "component:encryption",
        "domain",
        "util:kmp",
        "util:text",
        "component:pictures",
        "database",
        "util",
        "util:paths"
    ),

    "component:texteditor" to listOf(
        "component:formatting-popup",
        "domain",
        "i18n",
        "util:collection",
        "util:design",
        "util:kmp",
        "util:platform",
        "util:text",
        "util"
    ),

    "composed:richcontent" to listOf(
        "component:texteditor",
        "domain",
        "i18n",
        "util:collection",
        "util:design",
        "util:kmp",
        "util:text",
        "component:encryption",
        "component:notebooks",
        "component:pictures",
        "database",
        "util",
        "domain-test"
    ),

    "database" to listOf(
        "domain",
        "util"
    ),

    "database-legacy" to listOf(
        "util"
    ),

    "desktop" to listOf(
        "component:encryption",
        "component:formatting-bar",
        "component:serialization",
        "component:sync",
        "component:texteditor",
        "composed:richcontent",
        "domain",
        "i18n",
        "util:async",
        "util:design",
        "util:encryption",
        "util:platform"
    ),

    "domain" to listOf(
        "util:encryption",
        "util:kmp",
        "util:async",
        "util:error",
        "util:text"
    ),

    "domain-test" to listOf(
        "domain",
        "util"
    ),

    "feature:backups" to listOf(
        "component:cloudservice",
        "component:notebooks",
        "component:pictures",
        "component:security",
        "component:serialization",
        "component:settings",
        "composed:richcontent",
        "database",
        "domain",
        "util",
        "util:design",
        "util:paths",
        "util:yaml",
        "domainTest"
    ),

    "feature:cloudaccounts" to listOf(
        "component:cloudservice",
        "component:security",
        "util",
        "util:design"
    ),

    "feature:cloudservice" to listOf(
        "component:cloudservice",
        "feature:settings",
        "util",
        "util:design"
    ),

    "feature:compare-notes" to listOf(
        "component:security",
        "domain",
        "util",
        "util:design"
    ),

    "feature:debug-settings" to listOf(
        "util:design",
        "component:encryption",
        "domain",
        "util"
    ),

    "feature:docs" to listOf(
        "component:settings",
        "util",
        "util:design"
    ),

    "feature:editor" to listOf(
        "component:formatting-bar",
        "component:settings",
        "util:collection",
        "util:design",
        "component:encryption",
        "component:pictures",
        "component:security",
        "component:texteditor",
        "composed:richcontent",
        "domain",
        "feature:notebooks",
        "util"
    ),

    "feature:encryption" to listOf(
        "component:serialization",
        "domain"
    ),

    "feature:logbook-viewer" to listOf(
        "util:design",
        "component:security",
        "domain",
        "util"
    ),

    "feature:multi-column-layout" to listOf(
        "util:design"
    ),

    "feature:notebooks" to listOf(
        "util:design",
        "component:security",
        "domain",
        "util"
    ),

    "feature:notes" to listOf(
        "domain",
        "feature:editor",
        "util",
        "util:design"
    ),

    "feature:onboarding" to listOf(
        "util:design",
        "composed:richcontent",
        "domain",
        "util"
    ),

    "feature:pictures" to listOf(
        "util:design",
        "domain-test",
        "component:pictures",
        "component:security",
        "composed:richcontent",
        "domain",
        "util"
    ),

    "feature:security" to listOf(
        "util:design",
        "domain",
        "util"
    ),

    "feature:settings" to listOf(
        "component:cloudservice",
        "component:encryption",
        "component:purchases",
        "component:security",
        "component:settings",
        "component:sync",
        "database",
        "domain",
        "feature:backups",
        "util",
        "util:design"
    ),

    "feature:sync" to listOf(
        "util:design",
        "component:security",
        "composed:richcontent",
        "domain",
        "util",
        "domain-test"
    ),

    "util" to listOf(
        "i18n",
        "util:analytics",
        "util:async",
        "util:date",
        "util:encryption",
        "util:error",
        "util:preferences",
        "util:text",
        "util:design"
    ),

    "util:analytics" to listOf(
        "util:preferences"
    ),

    "util:date" to listOf(
        "i18n"
    ),

    "util:encryption" to listOf(
        "util:async",
        "util:random"
    ),

    "util:paths" to listOf(
        "util:text"
    ),

    "util:platform" to listOf(
        "util:kmp"
    ),

    "util:preferences" to listOf(
        "util:date"
    ),

    "web" to listOf(
        "component:serialization",
        "util:async",
        "util:date",
        "util:encryption",
        "util:error",
        "util:paths",
        "util:text",
        "util:yaml",
        "component:sync",
        "composed:richcontent",
        "domain"
    ),

    "web-compose" to listOf(
        "component:encryption",
        "component:formatting-bar",
        "component:serialization",
        "component:texteditor",
        "composed:richcontent",
        "domain",
        "feature:multi-column-layout",
        "util:design"
    )

)