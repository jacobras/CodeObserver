package nl.jacobras.codebaseobserver.server.graph

object GraphVisualizer {

    fun build(
        modules: Map<String, List<String>>,
        startModule: String = "",
        startModuleColor: String = "#a5a5b2",
        groupThreshold: Int
    ): String {
        val filteredModules = if (startModule.isNotEmpty()) {
            filterModules(modules = modules, startModule = startModule)
        } else {
            modules
        }

        val groups = getPossibleModuleGroups(filteredModules)
            .filter { it.value.size >= groupThreshold }
            .toMutableMap()

        val outputModules = mutableListOf<String>()
        val outputGroups = mutableListOf<String>()
        val outputDependencies = mutableListOf<Pair<String, String>>()

        for (group in groups) {
            outputGroups += group.key
        }

        for ((module, dependencies) in filteredModules) {
            if (outputGroups.none { module.startsWith(it) }) {
                outputModules += module
            }

            val groupForThisModule = groups.filter { module.startsWith(it.key) }.keys.firstOrNull()
            val aNameToUse = if (groupForThisModule != null) {
                "group$groupForThisModule"
            } else {
                module
            }

            for (dep in dependencies) {
                val groupForThisDep = groups.filter { dep.startsWith(it.key) }.keys.firstOrNull()
                val bNameToUse = if (groupForThisDep != null) {
                    "group$groupForThisDep"
                } else {
                    dep
                }

                if (aNameToUse != bNameToUse && !outputDependencies.contains(aNameToUse to bNameToUse)) {
                    outputDependencies += aNameToUse to bNameToUse
                }
            }
        }

        if (outputModules.size > 30) {
            return "graph TD\n    A[Too large: ${outputModules.size} modules.]"
        }

        return buildString {
            appendLine("graph TD")

            if (outputModules.isEmpty() && outputGroups.isEmpty()) {
                appendLine("    A[No data to display]")
            }

            for (module in outputModules) {
                appendLine("    $module")
            }

            for (group in outputGroups) {
                val numberOfModulesInGroup = filteredModules
                    .flatMap { listOf(it.key) + it.value }
                    .distinct()
                    .filter { it.startsWith(group) }
                    .size

                appendLine("    subgraph group$group [\"$group\"]")
                appendLine("        GROUP$group[\"$numberOfModulesInGroup modules\"]")
                appendLine("    end")
            }

            appendLine()
            appendLine("%% Dependencies")

            for ((a, b) in outputDependencies) {
                appendLine("    $a --> $b")
            }

            if (startModule.isNotEmpty()) {
                appendLine()
                appendLine("class $startModule start")
                appendLine("classDef start fill:$startModuleColor;")
            }
        }.trim()
    }

    /**
     * Filters [modules] to only include modules that are reachable from [startModule].
     */
    private fun filterModules(
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

    private fun getPossibleModuleGroups(modules: Map<String, List<String>>): Map<String, List<String>> {
        val possibleModuleGroups = mutableMapOf<String, MutableList<String>>()

        for (module in modules.keys) {
            val parts = module.split(":").filter { it.isNotEmpty() }
            for (i in 1 until parts.size) {
                val prefix = parts.take(i).joinToString(":")
                possibleModuleGroups.getOrPut(prefix) { mutableListOf() }.add(module)
            }
        }

        // Sort groups by size descending (largest first)
        return possibleModuleGroups.toList()
            .sortedByDescending { (_, group) -> group.size }
            .toMap()
    }
}