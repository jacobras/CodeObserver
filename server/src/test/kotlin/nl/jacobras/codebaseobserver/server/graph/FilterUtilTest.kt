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

        val groups = FilterUtil.getPossibleModuleGroups(input)

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

        val groups = FilterUtil.getPossibleModuleGroups(input)

        assertThat(groups).hasSize(1)
        assertThat(groups.toList()[0].first).isEqualTo("component")
    }
}