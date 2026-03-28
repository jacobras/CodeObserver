package nl.jacobras.codebaseobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationProgressDataSource
import nl.jacobras.codebaseobserver.dto.GitHash
import nl.jacobras.codebaseobserver.dto.MigrationId
import nl.jacobras.codebaseobserver.dto.MigrationProgressDto
import nl.jacobras.codebaseobserver.util.data.NetworkError
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val now = Clock.System.now()
private val DEMO_MIGRATION_PROGRESS = listOf(
    MigrationProgressDto(
        migrationId = MigrationId(1),
        gitHash = GitHash("a1b2c3d"),
        gitDate = now.minus(250.days),
        count = 120
    ),
    MigrationProgressDto(
        migrationId = MigrationId(1),
        gitHash = GitHash("b2c3d4e"),
        gitDate = now.minus(100.days),
        count = 94
    ),
    MigrationProgressDto(
        migrationId = MigrationId(1),
        gitHash = GitHash("c3d4e5f"),
        gitDate = now.minus(40.days),
        count = 67
    ),
    MigrationProgressDto(
        migrationId = MigrationId(1),
        gitHash = GitHash("d4e5f6a"),
        gitDate = now.minus(20.days),
        count = 38
    ),
    MigrationProgressDto(
        migrationId = MigrationId(1),
        gitHash = GitHash("e5f6a7b"),
        gitDate = now.minus(5.days),
        count = 15
    ),
    MigrationProgressDto(
        migrationId = MigrationId(2),
        gitHash = GitHash("a1b2c3d"),
        gitDate = now.minus(250.days),
        count = 45
    ),
    MigrationProgressDto(
        migrationId = MigrationId(2),
        gitHash = GitHash("b2c3d4e"),
        gitDate = now.minus(100.days),
        count = 32
    ),
    MigrationProgressDto(
        migrationId = MigrationId(2),
        gitHash = GitHash("c3d4e5f"),
        gitDate = now.minus(40.days),
        count = 18
    ),
    MigrationProgressDto(
        migrationId = MigrationId(2),
        gitHash = GitHash("d4e5f6a"),
        gitDate = now.minus(20.days),
        count = 7
    ),
    MigrationProgressDto(
        migrationId = MigrationId(2),
        gitHash = GitHash("e5f6a7b"),
        gitDate = now.minus(5.days),
        count = 0
    ),
)

internal class DemoMigrationProgressDataSource : MigrationProgressDataSource {
    override suspend fun fetchProgress(migrationId: MigrationId): Result<List<MigrationProgressDto>, NetworkError> =
        Ok(DEMO_MIGRATION_PROGRESS.filter { it.migrationId == migrationId })
}