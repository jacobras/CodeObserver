package nl.jacobras.codebaseobserver.server.graph

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlin.test.Test

class FilterUtilTest {

    @Test
    fun groups() {
        val input = mapOf(
            "app" to listOf(
                "module:some-thing:common",
                "module:some-thing:special",
                "module:some-thing:theme",
                "module:some-thing:special"
            ),
            "module:some-thing:common" to emptyList(),
            "module:some-thing:special" to emptyList(),
            "module:some-thing:theme" to emptyList()
        )

        val groups = FilterUtil.getPossibleModuleGroups(input, startModule = "")

        assertThat(groups).hasSize(1)
        assertThat(groups.toList()[0].first).isEqualTo("module:some-thing")
    }

    @Test
    fun groups2() {
        val input = mapOf(
            "component:cloudservice" to emptyList(),
            "component:security" to emptyList(),
            "feature:cloudaccounts" to listOf(
                "component:cloudservice",
                "component:security",
                "util",
                "util:design"
            ),
            "util" to emptyList(),
            "util:design" to emptyList()
        )

        val groups = FilterUtil.getPossibleModuleGroups(input, startModule = "")

        assertThat(groups).hasSize(1)
        assertThat(groups.toList()[0].first).isEqualTo("component")
    }

    @Test
    fun `startModule should never be grouped`() {
        val input = mapOf(
            "module:a" to emptyList(),
            "module:b" to listOf(
                "module:a",
                "module:b",
                "module:c"
            ),
            "module:c" to emptyList()
        )

        val groups = FilterUtil.getPossibleModuleGroups(input, startModule = "module:b")

        assertThat(groups).hasSize(1)
        assertThat(groups.toList()[0].first).isEqualTo("module")
        assertThat(groups.toList()[0].second).isEqualTo(listOf("module:a", "module:c"))
    }

    @Test
    fun `group api-impl modules`() {
        val input = mapOf<String, List<String>>(
            "feature:foryou:api" to emptyList(),
            "feature:foryou:impl" to emptyList(),
            "feature:interests:api" to emptyList(),
            "feature:interests:impl" to emptyList(),
            "feature:bookmarks:api" to emptyList(),
            "feature:bookmarks:impl" to emptyList(),
            "feature:topic:api" to emptyList(),
            "feature:topic:impl" to emptyList(),
            "feature:search:api" to emptyList(),
            "feature:search:impl" to emptyList()
        )

        val groups = FilterUtil.getPossibleModuleGroups(input, startModule = "")

        assertThat(groups).hasSize(1)
        assertThat(groups.toList()[0].first).isEqualTo("feature")
        assertThat(groups.toList()[0].second).isEqualTo(listOf("module:a", "module:c"))
    }
}