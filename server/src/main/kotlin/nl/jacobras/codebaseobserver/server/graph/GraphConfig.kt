package nl.jacobras.codebaseobserver.server.graph

sealed interface GraphConfig {
    data class DeprecatedModule(val module: String) : GraphConfig {
        fun matches(module: String) = module.matchesWildcard(this.module)
    }

    data class ForbiddenDependency(val a: String, val b: String) : GraphConfig {
        fun matches(a: String, b: String) = a.matchesWildcard(this.a) && b.matchesWildcard(this.b)
    }
}

private fun String.matchesWildcard(pattern: String): Boolean {
    if (!pattern.contains('*')) return this == pattern
    val regex = pattern
        .split('*')
        .joinToString(separator = ".*") { Regex.escape(it) }
    return Regex("^$regex$").matches(this)
}