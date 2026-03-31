package nl.jacobras.codeobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesDataSource
import nl.jacobras.codeobserver.dto.BuildTimeDto
import nl.jacobras.codeobserver.dto.GitHash
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private val now = Clock.System.now()
private val DEMO_BUILD_TIMES = listOf(
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleDebug",
        gitHash = GitHash("a1b2c3d"),
        gitDate = now.minus(110.days),
        timeSeconds = 95
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleRelease",
        gitHash = GitHash("a1b2c3d"),
        gitDate = now.minus(110.days),
        timeSeconds = 485
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleDebug",
        gitHash = GitHash("b2c3d4e"),
        gitDate = now.minus(50.days),
        timeSeconds = 88
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleRelease",
        gitHash = GitHash("b2c3d4e"),
        gitDate = now.minus(50.days),
        timeSeconds = 378
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleDebug",
        gitHash = GitHash("c3d4e5f"),
        gitDate = now.minus(28.days),
        timeSeconds = 102
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleRelease",
        gitHash = GitHash("c3d4e5f"),
        gitDate = now.minus(24.days),
        timeSeconds = 495
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleDebug",
        gitHash = GitHash("d4e5f6a"),
        gitDate = now.minus(10.days),
        timeSeconds = 91
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleRelease",
        gitHash = GitHash("d4e5f6a"),
        gitDate = now.minus(5.days),
        timeSeconds = 482
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleDebug",
        gitHash = GitHash("e5f6a7b"),
        gitDate = now.minus(3.days),
        timeSeconds = 87
    ),
    BuildTimeDto(
        projectId = ProjectId("myApp"),
        buildName = "assembleRelease",
        gitHash = GitHash("e5f6a7b"),
        gitDate = now.minus(5.hours),
        timeSeconds = 374
    ),
)

internal class DemoBuildTimesDataSource : BuildTimesDataSource {
    override suspend fun fetchBuildTimes(projectId: ProjectId): Result<List<BuildTimeDto>, NetworkError> =
        Ok(DEMO_BUILD_TIMES.filter { it.projectId == projectId })
}