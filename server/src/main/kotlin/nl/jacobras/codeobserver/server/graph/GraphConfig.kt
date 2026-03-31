package nl.jacobras.codeobserver.server.graph

sealed interface GraphConfig {
    data class DeprecatedModule(val module: String) : GraphConfig {
        private val regex by lazy { wildcardToRegex(module) }
        fun matches(module: String) = regex.matches(module)
    }

    data class ForbiddenDependency(val a: String, val b: String) : GraphConfig {
        private val regexA by lazy { wildcardToRegex(a) }
        private val regexB by lazy { wildcardToRegex(b) }
        fun matches(a: String, b: String) = regexA.matches(a) && regexB.matches(b)
    }
}

private fun wildcardToRegex(pattern: String): Regex {
    if (!pattern.contains('*')) return Regex.fromLiteral(pattern)
    val regex = pattern
        .split('*')
        .joinToString(separator = ".*") { Regex.escape(it) }
    return Regex("^$regex$")
}