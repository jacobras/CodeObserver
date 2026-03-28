package nl.jacobras.codebaseobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactSizesDataSource
import nl.jacobras.codebaseobserver.dto.ArtifactSizeDto
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private val now = Clock.System.now()
private val DEMO_ARTIFACT_SIZES = listOf(
    ArtifactSizeDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(80.days),
        name = "app-release.apk",
        semVer = "1.0.0",
        size = 18_450_000L
    ),
    ArtifactSizeDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(63.days),
        name = "app-release.apk",
        semVer = "1.1.0",
        size = 19_100_000L
    ),
    ArtifactSizeDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(63.days),
        name = "app-debug.apk",
        semVer = "1.1.0",
        size = 38_100_000L
    ),
    ArtifactSizeDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(30.days),
        name = "app-release.apk",
        semVer = "1.2.0",
        size = 19_650_000L
    ),
    ArtifactSizeDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(5.days),
        name = "app-release.apk",
        semVer = "2.0.0",
        size = 21_200_000L
    ),
    ArtifactSizeDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(20.hours),
        name = "app-release.apk",
        semVer = "2.1.0",
        size = 21_800_000L
    ),
    ArtifactSizeDto(
        projectId = ProjectId("myApp"),
        createdAt = now.minus(20.hours),
        name = "app-debug.apk",
        semVer = "2.1.0",
        size = 40_800_000L
    ),
)

internal class DemoArtifactSizesDataSource : ArtifactSizesDataSource {
    override suspend fun fetchArtifactSizes(projectId: ProjectId): Result<List<ArtifactSizeDto>, NetworkError> =
        Ok(DEMO_ARTIFACT_SIZES.filter { it.projectId == projectId })
}