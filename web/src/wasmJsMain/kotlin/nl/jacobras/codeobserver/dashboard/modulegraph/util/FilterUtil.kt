package nl.jacobras.codeobserver.dashboard.modulegraph.util

internal object FilterUtil {

    fun getPossibleModuleGroups(
        modules: Map<String, List<String>>,
        startModule: String
    ): Map<String, List<String>> {
        val possibleModuleGroups = mutableMapOf<String, MutableList<String>>()

        for (module in modules.keys) {
            if (module == startModule) continue // startModule stays separate, never grouped

            val parts = module.split(":").filter { it.isNotEmpty() }
            for (i in 1 until parts.size) {
                val prefix = parts.take(i).joinToString(":")
                possibleModuleGroups.getOrPut(prefix) { mutableListOf() }.add(module)
            }
        }

        // Remove child-group members from parent groups
        // Sort prefixes longest-first so we process children before parents
        val sortedPrefixes = possibleModuleGroups.keys.sortedByDescending { it.length }

        for (prefix in sortedPrefixes) {
            val members = possibleModuleGroups[prefix] ?: continue
            // Find any sub-groups of this prefix that are valid groups (size > 1)
            val childGroups = sortedPrefixes.filter { other ->
                other != prefix && other.startsWith("$prefix:") && (possibleModuleGroups[other]?.size ?: 0) > 1
            }
            // Remove from this group any module already claimed by a child group
            for (child in childGroups) {
                val childMembers = possibleModuleGroups[child] ?: continue
                members.removeAll(childMembers.toSet())
            }
            if (members.isEmpty()) possibleModuleGroups.remove(prefix)
        }

        return possibleModuleGroups.toList()
            .filter { (_, group) -> group.size > 1 }
            .sortedByDescending { (_, group) -> group.size }
            .toMap()
    }

    /**
     * Filters [modules] to only include modules that are reachable from [startModule].
     */
    fun filterByStartModule(
        modules: Map<String, List<String>>,
        startModule: String
    ): Map<String, List<String>> {
        val reachable = mutableSetOf<String>()
        val stack = ArrayDeque<String>()
        stack.add(startModule)

        // Collect all reachable modules
        while (stack.isNotEmpty()) {
            val module = stack.removeLast()
            if (module !in reachable) {
                reachable.add(module)
                modules[module]?.let { stack.addAll(it) }
            }
        }

        // Build filtered map
        return modules
            .filterKeys { it in reachable }
            .mapValues { (_, deps) -> deps.filter { it in reachable } }
    }

    /**
     * Filters [modules] to only include modules that are at depth [layerDepth] from [startModule].
     */
    fun filterByLayerDepth(
        modules: Map<String, List<String>>,
        layerDepth: Int,
        startModule: String
    ): Map<String, List<String>> {
        if (layerDepth == 0) return modules

        // BFS to find all modules within [layerDepth] distance from [startModule]
        val reachable = mutableSetOf<String>()
        var currentLevel = setOf(startModule)
        reachable.add(startModule)

        repeat(layerDepth) { iteration ->
            val nextLevel = mutableSetOf<String>()
            for (module in currentLevel) {
                val dependencies = modules[module] ?: emptyList()
                for (dep in dependencies) {
                    if (dep !in reachable) {
                        nextLevel.add(dep)
                        reachable.add(dep)
                    }
                }
            }
            currentLevel = nextLevel
        }

        // [currentLevel] now holds the modules at exactly layerDepth
        return modules
            .filter { (key, _) -> key in reachable }
            .mapValues { (key, deps) ->
                if (key in currentLevel) emptyList() else deps
            }
    }
}