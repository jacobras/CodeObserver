package nl.jacobras.codebaseobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphSettingsDataSource
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingDto
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingId
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val now = Clock.System.now()

internal class DemoModuleGraphSettingsDataSource : ModuleGraphSettingsDataSource {
    private val settings = mutableListOf(
        ModuleGraphSettingDto(
            id = ModuleGraphSettingId(1),
            createdAt = now.minus(300.days),
            projectId = ProjectId("myApp"),
            type = "deprecatedModule",
            data = "util"
        ),
        ModuleGraphSettingDto(
            id = ModuleGraphSettingId(2),
            createdAt = now.minus(400.days),
            projectId = ProjectId("myApp"),
            type = "forbiddenDependency",
            data = "*feature:* -> *feature:*"
        )
    )

    override suspend fun fetchSettings(projectId: ProjectId): Result<List<ModuleGraphSettingDto>, NetworkError> =
        Ok(settings.filter { it.projectId == projectId })

    override suspend fun create(projectId: ProjectId, type: String, data: String): Result<Unit, NetworkError> {
        settings.add(
            ModuleGraphSettingDto(
                id = ModuleGraphSettingId(settings.maxOf { it.id.value } + 1),
                createdAt = Clock.System.now(),
                projectId = projectId,
                type = type,
                data = data
            )
        )
        return Ok(Unit)
    }

    override suspend fun update(id: ModuleGraphSettingId, type: String, data: String): Result<Unit, NetworkError> {
        val index = settings.indexOfFirst { it.id == id }
        if (index >= 0) {
            settings[index] = settings[index].copy(type = type, data = data)
        }
        return Ok(Unit)
    }

    override suspend fun delete(id: ModuleGraphSettingId): Result<Unit, NetworkError> {
        settings.removeAll { it.id == id }
        return Ok(Unit)
    }
}