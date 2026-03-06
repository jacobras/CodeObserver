package nl.jacobras.codebaseobserver.server.graph

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class GraphConfigTest {

    @Test
    fun `deprecated module`() {
        val config = GraphConfig.DeprecatedModule("com.example.foo")
        assertThat(config.matches("com.example.foo")).isTrue()
        assertThat(config.matches("com.example.bar")).isFalse()
    }

    @Test
    fun `deprecated module with simple wildcard`() {
        val config = GraphConfig.DeprecatedModule("com.example.*")
        assertThat(config.matches("com.example.foo")).isTrue()
        assertThat(config.matches("com.example.bar")).isTrue()
        assertThat(config.matches("com.example")).isFalse()
    }

    @Test
    fun `forbidden dependency module with wildcard`() {
        val config = GraphConfig.ForbiddenDependency("feature:*", "feature:*")
        assertThat(config.matches("feature:a", "feature:b")).isTrue()
        assertThat(config.matches("feature:a", "util")).isFalse()
    }

    @Test
    fun `forbidden dependency module with deep wildcard`() {
        val config = GraphConfig.ForbiddenDependency("*:feature:*", "*:feature:*")
        assertThat(config.matches("domain:feature:a", "domain:feature:b")).isTrue()
        assertThat(config.matches("domain:feature:a", "util")).isFalse()
    }

    @Test
    fun `forbidden dependency module with broad wildcard`() {
        val config = GraphConfig.ForbiddenDependency("*feature*", "*feature*")
        assertThat(config.matches("domain:feature:a", "domain:feature:b")).isTrue()
        assertThat(config.matches("domain:feature:a", "util")).isFalse()
    }
}