package nl.jacobras.codebaseobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codebaseobserver.dashboard.trends.TrendsDataSource
import nl.jacobras.codebaseobserver.dto.CodeMetricsDto
import nl.jacobras.codebaseobserver.dto.GitHash
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private val now = Clock.System.now()
private val DEMO_METRICS = mutableListOf(
    CodeMetricsDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(300.days),
        gitHash = GitHash("a1b2c3d"),
        gitDate = now.minus(300.days),
        linesOfCode = 40000,
        moduleCount = 18,
        moduleTreeHeight = 5
    ),
    CodeMetricsDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(90.days),
        gitHash = GitHash("b2c3d4e"),
        gitDate = now.minus(90.days),
        linesOfCode = 47500,
        moduleCount = 20,
        moduleTreeHeight = 5
    ),
    CodeMetricsDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(14.days),
        gitHash = GitHash("c3d4e5f"),
        gitDate = now.minus(14.days),
        linesOfCode = 51200,
        moduleCount = 24,
        moduleTreeHeight = 6
    ),
    CodeMetricsDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(7.days),
        gitHash = GitHash("d4e5f6a"),
        gitDate = now.minus(7.days),
        linesOfCode = 53100,
        moduleCount = 21,
        moduleTreeHeight = 6
    ),
    CodeMetricsDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(3.hours),
        gitHash = GitHash("e5f6a7b"),
        gitDate = now.minus(3.hours),
        linesOfCode = 56800,
        moduleCount = 25,
        moduleTreeHeight = 4
    ),
)

internal class DemoTrendsDataSource : TrendsDataSource {
    override suspend fun fetchMetrics(projectId: ProjectId): Result<List<CodeMetricsDto>, NetworkError> {
        return Ok(DEMO_METRICS.filter { it.projectId == projectId })
    }

    override suspend fun delete(projectId: ProjectId, gitHash: GitHash): Result<Unit, NetworkError> {
        DEMO_METRICS.removeAll { it.projectId == projectId && it.gitHash == gitHash }
        return Ok(Unit)
    }
}