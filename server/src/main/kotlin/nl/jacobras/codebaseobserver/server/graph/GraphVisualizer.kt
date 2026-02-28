package nl.jacobras.codebaseobserver.server.graph

import nl.jacobras.codebaseobserver.server.graph.FilterUtil.filterByLayerDepth
import nl.jacobras.codebaseobserver.server.graph.FilterUtil.filterByStartModule
import nl.jacobras.codebaseobserver.server.graph.FilterUtil.getPossibleModuleGroups

object GraphVisualizer {

    fun build(
        modules: Map<String, List<String>>,
        startModule: String = "",
        startModuleColor: String = "#a5a5b2",
        groupThreshold: Int,
        nodeLimit: Int = 30,
        layerDepth: Int = 30
    ): String {
        val filteredModules = if (startModule.isNotEmpty()) {
            val res = filterByStartModule(modules = modules, startModule = startModule)
            filterByLayerDepth(res, layerDepth, startModule)
        } else {
            modules
        }

        val groups = getPossibleModuleGroups(filteredModules, startModule = startModule)
            .filter { it.value.size >= groupThreshold }
            .toMutableMap()

        val outputModules = mutableListOf<String>()
        val outputGroups = mutableListOf<String>()
        val outputDependencies = mutableListOf<Pair<String, String>>()

        for (group in groups) {
            outputGroups += group.key
        }

        for ((module, dependencies) in filteredModules) {
            if (module == startModule || outputGroups.none { module.startsWith(it) }) {
                outputModules += module
            }

            val groupForThisModule = groups.filter { module.startsWith(it.key) }.keys.firstOrNull()
            val aNameToUse = if (module != startModule && groupForThisModule != null) {
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

        val nodeCount = outputModules.size + outputGroups.size
        if (nodeCount > nodeLimit) {
            return "graph TD\n    A[\"Too large: $nodeCount nodes (limit $nodeLimit).\"]"
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
}