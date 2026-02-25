package nl.jacobras.codebaseobserver.server.graph

internal object GraphBuilder {

    /**
     * Builds a Mermaid graph based on the provided modules and their dependencies.
     *
     * @param modules A map of module names to their dependencies.
     * @param startModule The module to start the graph from. If empty, the graph will start at the root.
     * @param groupThreshold The threshold for grouping modules. If the number of modules exceeds this threshold,
     * they will be grouped by their prefix (e.g., "module:submodule" will be grouped as "module").
     */
    fun build(
        modules: Map<String, List<String>>,
        startModule: String = "",
        startModuleColor: String = "#a5a5b2",
        groupThreshold: Int = 30
    ): String {

        // 1️⃣ Filter participating modules
        val filtered = if (startModule.isBlank()) {
            modules
        } else {
            val reachable = mutableSetOf<String>()
            val stack = ArrayDeque<String>()
            stack.add(startModule)

            while (stack.isNotEmpty()) {
                val current = stack.removeLast()
                if (!reachable.add(current)) {
                    continue
                }
                modules[current]?.forEach(stack::add)
            }

            modules
                .filterKeys { it in reachable }
                .mapValues { (_, deps) -> deps.filter { it in reachable } }
        }

        // 2️⃣ Decide grouping based on total nodes
        val totalNodes = (filtered.keys + filtered.values.flatten()).toSet()
        val shouldGroup = totalNodes.size > groupThreshold

        // Track group -> original modules
        val groupContents = mutableMapOf<String, MutableSet<String>>()

        fun group(name: String): String {
            return if (shouldGroup) {
                val groupName = name.substringBefore(":")
                groupContents.getOrPut(groupName) { mutableSetOf() }.add(name)
                groupName
            } else {
                name
            }
        }

        // 3️⃣ Build grouped structure
        val grouped = mutableMapOf<String, MutableSet<String>>()
        for ((module, deps) in filtered) {
            val group = group(module)
            val set = grouped.getOrPut(group) { mutableSetOf() }
            deps.forEach { dep -> set.add(group(dep)) }
        }

        // 4️⃣ Prepare distinct node labels
        val distinctNodes = grouped.keys

        fun nodeLabel(node: String): String {
            return if (shouldGroup && (groupContents[node]?.size ?: 0) > 1) {
                """$node["$node<br>(${groupContents[node]?.size} modules)"]"""
            } else node
        }

        val groupedStartModule = if (startModule.isNotBlank()) group(startModule) else null

        // 5️⃣ Render graph
        return buildString {
            appendLine("graph TD")

            // Render nodes
            distinctNodes.forEach { node ->
                appendLine("    ${nodeLabel(node)}")
            }

            appendLine()
            appendLine("%% Dependencies")

            // Render dependencies
            grouped.forEach { (module, deps) ->
                deps.forEach { dep ->
                    if (module != dep) appendLine("    ${group(module)} --> ${group(dep)}")
                }
            }

            // Render styling
            groupedStartModule?.let {
                appendLine()
                appendLine("class $it start")
                appendLine("classDef start fill:$startModuleColor;")
            }
        }.trim()
    }
}