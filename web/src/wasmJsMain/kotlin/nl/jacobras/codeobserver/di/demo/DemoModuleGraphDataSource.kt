package nl.jacobras.codeobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphDataSource
import nl.jacobras.codeobserver.dto.GraphModuleDto
import nl.jacobras.codeobserver.dto.GraphModulesDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError

private val DEMO_GRAPH_MODULES = GraphModulesDto(
    longestPath = listOf(":app", ":feature:home", ":core:data", ":core:network"),
    modules = listOf(
        GraphModuleDto(name = ":app", score = 0),
        GraphModuleDto(name = ":feature:home", score = 15),
        GraphModuleDto(name = ":feature:profile", score = 4),
        GraphModuleDto(name = ":feature:settings", score = 20),
        GraphModuleDto(name = ":core:network", score = 5),
        GraphModuleDto(name = ":core:data", score = 0),
        GraphModuleDto(name = ":core:ui", score = 3),
        GraphModuleDto(name = ":core:common", score = 24),
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

    override suspend fun fetchGraph(
        projectId: ProjectId,
        startModule: String,
        groupingThreshold: Int,
        layerDepth: Int
    ): Result<String, NetworkError> {
        return Ok(
            """
            graph TD
                app
                core:common
                core:data
                core:network
                core:ui
                feature:home
                feature:profile
                feature:settings
            
            %% Dependencies
                app --> core:common
                app --> core:data
                app --> core:ui
                app --> feature:home
                app --> feature:profile
                app --> feature:settings
                feature:home --> core:data
                core:data --> core:network
                feature:profile --> core:data
                feature:settings --> core:data
                core:ui --> core:common
            
            classDef moduleType0 fill:#9bf6ff;
            classDef moduleType1 fill:#bdb2ff;
            classDef moduleType2 fill:#caffbf;
            class app moduleType0
            class core:common moduleType1
            class core:network moduleType1
            class feature:home moduleType2
            class feature:profile moduleType2
            class feature:settings moduleType2
            """.trimIndent()
        )
    }
}