package nl.jacobras.codeobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.util.GraphConfig
import nl.jacobras.codeobserver.dto.GitHash
import nl.jacobras.codeobserver.dto.GradleDto
import nl.jacobras.codeobserver.dto.GradleMetricPointDto
import nl.jacobras.codeobserver.dto.GraphConfigDto
import nl.jacobras.codeobserver.dto.GraphModuleDto
import nl.jacobras.codeobserver.dto.GraphVisualInfoDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private val now = Clock.System.now()
private val DEMO_GRAPH_MODULES = GradleDto(
    longestPath = listOf(":app", ":feature:home", ":core:data", ":core:network"),
    modules = listOf(
        GraphModuleDto(name = "app", score = 0),
        GraphModuleDto(name = "core", score = 5),
        GraphModuleDto(name = "core:common", score = 0),
        GraphModuleDto(name = "core:data", score = 2),
        GraphModuleDto(name = "core:network", score = 1),
        GraphModuleDto(name = "core:ui", score = 2),
        GraphModuleDto(name = "domain", score = 1),
        GraphModuleDto(name = "feature:home", score = 0),
        GraphModuleDto(name = "feature:profile", score = 0),
        GraphModuleDto(name = "feature:settings", score = 5)
    ),
    metrics = listOf(
        GradleMetricPointDto(gitHash = GitHash("demo0001"), gitDate = now.minus(300.days), moduleCount = 18, moduleTreeHeight = 5),
        GradleMetricPointDto(gitHash = GitHash("demo0002"), gitDate = now.minus(90.days), moduleCount = 20, moduleTreeHeight = 5),
        GradleMetricPointDto(gitHash = GitHash("demo0003"), gitDate = now.minus(14.days), moduleCount = 24, moduleTreeHeight = 6),
        GradleMetricPointDto(gitHash = GitHash("demo0004"), gitDate = now.minus(7.days), moduleCount = 21, moduleTreeHeight = 6),
        GradleMetricPointDto(gitHash = GitHash("demo0005"), gitDate = now.minus(3.hours), moduleCount = 25, moduleTreeHeight = 4)
    )
)

internal class DemoModuleGraphDataSource : ModuleGraphDataSource {
    override suspend fun fetchGraphModules(
        projectId: ProjectId,
        sortOrder: ModuleSortOrder,
        gitHash: GitHash?
    ): Result<GradleDto, NetworkError> {
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
        projectId: ProjectId,
        gitHash: GitHash?
    ): Result<GraphVisualInfoDto, NetworkError> {
        return Ok(
            GraphVisualInfoDto(
                modules = mapOf(
                    "app" to listOf(
                        "core",
                        "core:common",
                        "core:data",
                        "core:ui",
                        "feature:home",
                        "feature:profile",
                        "feature:settings"
                    ),
                    "core" to listOf(
                        "core:common",
                        "core:data",
                        "core:network"
                    ),
                    "core:common" to emptyList(),
                    "core:data" to listOf(
                        "core:network"
                    ),
                    "core:network" to listOf(
                        "core:common"
                    ),
                    "core:ui" to listOf(
                        "core:common"
                    ),
                    "domain" to listOf(
                        "core:common",
                    ),
                    "feature:home" to listOf(
                        "core:ui",
                        "feature:settings"
                    ),
                    "feature:profile" to listOf(
                        "core:data",
                        "core:ui"
                    ),
                    "feature:settings" to listOf(
                        "core",
                        "domain"
                    )
                ),
                moduleColors = mapOf(
                    "app" to "#9bf6ff",
                    "core:common" to "#bdb2ff",
                    "core:network" to "#bdb2ff",
                    "feature:home" to "#caffbf",
                    "feature:profile" to "#caffbf",
                    "feature:settings" to "#caffbf"
                ),
                config = listOf(
                    GraphConfigDto.DeprecatedModule("core"),
                    GraphConfigDto.ForbiddenDependency("*feature:*", "*feature:*")
                )
            )
        )
    }
}