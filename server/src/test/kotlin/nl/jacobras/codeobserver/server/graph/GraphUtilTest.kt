package nl.jacobras.codeobserver.server.graph

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GraphUtilTest {

    @Test
    fun `calculate betweenness centrality score`() {
        val modules = mapOf(
            "app" to listOf("feature1", "feature2", "feature3", "domain"),
            "feature1" to listOf("feature2", "domain"),
            "feature2" to listOf("domain"),
            "feature3" to listOf("feature2", "domain"),
            "domain" to listOf("util:text"),
            "util:text" to listOf("util"),
            "util" to emptyList()
        )

        val scores = GraphUtil.calculateBetweennessCentralityScore(modules)

        assertThat(scores).isEqualTo(
            mapOf(
                "app" to 0.0,
                "domain" to 8.0,
                "feature1" to 0.0,
                "feature2" to 0.0,
                "feature3" to 0.0,
                "util" to 0.0,
                "util:text" to 5.0
            )
        )
    }
}