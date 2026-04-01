package nl.jacobras.codeobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphDataSource
import nl.jacobras.codeobserver.dto.GraphModuleDto
import nl.jacobras.codeobserver.dto.GraphModulesDto
import nl.jacobras.codeobserver.dto.GraphVisualInfoDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError

private val DEMO_GRAPH_MODULES = GraphModulesDto(
    longestPath = listOf(":app", ":feature:home", ":core:data", ":core:network"),
    modules = listOf(
        GraphModuleDto(name = "app", score = 0),
        GraphModuleDto(name = "feature:home", score = 15),
        GraphModuleDto(name = "feature:profile", score = 4),
        GraphModuleDto(name = "feature:settings", score = 20),
        GraphModuleDto(name = "core:network", score = 5),
        GraphModuleDto(name = "core:data", score = 0),
        GraphModuleDto(name = "core:ui", score = 3),
        GraphModuleDto(name = "core:common", score = 24),
    )
)

internal class DemoModuleGraphDataSource : ModuleGraphDataSource {
    override suspend fun fetchGraphModules(
        projectId: ProjectId,
        sortOrder: ModuleSortOrder
    ): Result<GraphModulesDto, NetworkError> {
        return when (sortOrder) {
            ModuleSortOrder.Alphabetical -> Ok(
                DEMO_GRAPH_MODULES.copy(
                    modules = DEMO_GRAPH_MODULES.modules
                        .sortedBy { it.name }
                        .map { it.copy(score = 0) }
                )
            )
            ModuleSortOrder.BetweennessCentrality -> Ok(
                DEMO_GRAPH_MODULES.copy(
                    modules = DEMO_GRAPH_MODULES.modules.sortedByDescending { it.score }
                )
            )
        }
    }

    override suspend fun fetchGraphInfo(
        projectId: ProjectId
    ): Result<GraphVisualInfoDto, NetworkError> {
        return Ok(
            GraphVisualInfoDto(
                modules = mapOf(
                    "app" to listOf(
                        "core:common",
                        "core:data",
                        "core:ui",
                        "feature:home",
                        "feature:profile",
                        "feature:settings"
                    ),
                    "core:common" to emptyList(),
                    "core:data" to listOf(
                        "core:network"
                    ),
                    "core:network" to emptyList(),
                    "core:ui" to listOf(
                        "core:common"
                    ),
                    "feature:home" to listOf(
                        "core:data"
                    ),
                    "feature:profile" to listOf(
                        "core:data"
                    ),
                    "feature:settings" to listOf(
                        "core:data"
                    )
                ),
                moduleColors = mapOf(
                    "app" to "#9bf6ff",
                    "core:common" to "#bdb2ff",
                    "core:network" to "#bdb2ff",
                    "feature:home" to "#caffbf",
                    "feature:profile" to "#caffbf",
                    "feature:settings" to "#caffbf"
                )
            )
        )
    }
}