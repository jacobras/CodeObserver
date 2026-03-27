package nl.jacobras.codebaseobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationsDataSource
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationId
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val now = Clock.System.now()

internal class DemoMigrationsDataSource : MigrationsDataSource {
    private val migrations = mutableListOf(
        MigrationDto(
            id = MigrationId(1),
            createdAt = now.minus(100.days),
            name = "RxJava to Coroutines",
            description = "Replace all RxJava usage with Kotlin Coroutines",
            projectId = ProjectId("myApp"),
            type = "import",
            rule = "io.reactivex"
        ),
        MigrationDto(
            id = MigrationId(2),
            createdAt = now.minus(10.days),
            name = "Split [util] module",
            description = "[Util] should be split into smaller modules",
            projectId = ProjectId("myApp"),
            type = "moduleUsage",
            rule = "util"
        ),
    )

    override suspend fun fetchMigrations(projectId: ProjectId): Result<List<MigrationDto>, NetworkError> =
        Ok(migrations.filter { it.projectId == projectId })

    override suspend fun create(
        projectId: ProjectId,
        name: String,
        description: String,
        type: String,
        rule: String
    ): Result<Unit, NetworkError> {
        migrations.add(
            MigrationDto(
                id = MigrationId(migrations.maxOf { it.id.value } + 1),
                createdAt = Clock.System.now(),
                name = name,
                description = description,
                projectId = projectId,
                type = type,
                rule = rule
            )
        )
        return Ok(Unit)
    }

    override suspend fun update(id: MigrationId, name: String, description: String): Result<Unit, NetworkError> {
        val index = migrations.indexOfFirst { it.id == id }
        if (index >= 0) {
            migrations[index] = migrations[index].copy(name = name, description = description)
        }
        return Ok(Unit)
    }

    override suspend fun delete(id: MigrationId): Result<Unit, NetworkError> {
        migrations.removeAll { it.id == id }
        return Ok(Unit)
    }
}