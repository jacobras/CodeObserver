package nl.jacobras.codeobserver.dashboard.modulegraph.util

import nl.jacobras.codeobserver.dashboard.modulegraph.util.FilterUtil.filterByLayerDepth
import nl.jacobras.codeobserver.dashboard.modulegraph.util.FilterUtil.filterByStartModule
import nl.jacobras.codeobserver.dashboard.modulegraph.util.FilterUtil.getPossibleModuleGroups

object GraphVisualizer {

    fun build(
        modules: Map<String, List<String>>,
        startModule: String = "",
        startModuleColor: String = "#a5a5b2",
        groupingThreshold: Int,
        nodeLimit: Int = 30,
        layerDepth: Int = 30,
        config: List<GraphConfig> = emptyList(),
        moduleColors: Map<String, String> = emptyMap()
    ): String {
        val filteredModules = if (startModule.isNotEmpty()) {
            val res = filterByStartModule(modules = modules, startModule = startModule)
            filterByLayerDepth(res, layerDepth, startModule)
        } else {
            modules
        }

        val groups = getPossibleModuleGroups(filteredModules, startModule = startModule)
            .filter { it.value.size >= groupingThreshold }
            .toMutableMap()

        val outputModules = mutableListOf<String>()
        val outputGroups = mutableListOf<String>()
        val outputDependencies = mutableListOf<Pair<String, String>>()

        for (group in groups) {
            outputGroups += group.key
        }

        val deprecatedDependencyIndices = mutableListOf<Int>()
        val forbiddenDependencyIndices = mutableListOf<Int>()

        for ((module, dependencies) in filteredModules) {
            if (module == startModule || outputGroups.none { module.startsWith(it) }) {
                outputModules += module
            }

            val groupForThisModule = groups
                .filter { module.startsWith(it.key) }
                .keys.maxOrNull() // Take the longest group, so we get the most specific one.
            val aNameToUse = if (module != startModule && groupForThisModule != null) {
                "group$groupForThisModule"
            } else {
                module
            }

            for (dep in dependencies) {
                val groupForThisDep = groups
                    .filter { dep.startsWith(it.key) }
                    .keys.maxOrNull() // Take the longest group, so we get the most specific one.
                val bNameToUse = if (groupForThisDep != null) {
                    "group$groupForThisDep"
                } else {
                    dep
                }

                if (aNameToUse != bNameToUse && !outputDependencies.contains(aNameToUse to bNameToUse)) {
                    outputDependencies += aNameToUse to bNameToUse

                    val currentDependencyIndex = outputDependencies.lastIndex
                    if (!forbiddenDependencyIndices.contains(currentDependencyIndex)
                        && config.any { it is GraphConfig.ForbiddenDependency && it.matches(module, dep) }
                    ) {
                        forbiddenDependencyIndices.add(currentDependencyIndex)
                    }
                    if (!deprecatedDependencyIndices.contains(currentDependencyIndex)
                        && config.any { it is GraphConfig.DeprecatedModule && it.matches(dep) }
                    ) {
                        deprecatedDependencyIndices.add(currentDependencyIndex)
                    }
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

            if (deprecatedDependencyIndices.isNotEmpty() || forbiddenDependencyIndices.isNotEmpty()) {
                appendLine()

                for (index in deprecatedDependencyIndices) {
                    appendLine("linkStyle $index stroke:orange,stroke-width:3px;")
                }
                for (index in forbiddenDependencyIndices) {
                    appendLine("linkStyle $index stroke:red,stroke-width:3px;")
                }
            }

            val applicableColors = moduleColors.filter { it.key in outputModules }
            if (applicableColors.isNotEmpty()) {
                appendLine()
                val colorGroups = applicableColors.entries.groupBy { it.value }
                colorGroups.entries.forEachIndexed { index, (color, entries) ->
                    val nodeList = entries.joinToString(",") { it.key }
                    appendLine("classDef moduleType$index fill:$color;")
                    appendLine("class $nodeList moduleType$index")
                }
            }
        }.trim()
    }
}