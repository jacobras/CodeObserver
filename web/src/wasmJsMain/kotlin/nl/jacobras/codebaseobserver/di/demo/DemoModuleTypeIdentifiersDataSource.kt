package nl.jacobras.codebaseobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleColors
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSource
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierDto
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierId
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class DemoModuleTypeIdentifiersDataSource : ModuleTypeIdentifiersDataSource {
    private var nextId = 5
    private val identifiers = mutableListOf(
        ModuleTypeIdentifierDto(
            ModuleTypeIdentifierId(1),
            ProjectId("myApp"),
            "App",
            "com.android.application",
            1,
            ModuleColors.Blue.hex
        ),
        ModuleTypeIdentifierDto(
            ModuleTypeIdentifierId(2),
            ProjectId("myApp"),
            "Feature",
            "myApp.convention.feature",
            2,
            ModuleColors.Green.hex
        ),
        ModuleTypeIdentifierDto(
            ModuleTypeIdentifierId(2),
            ProjectId("myApp"),
            "Library",
            "myApp.convention.library",
            3,
            ModuleColors.Green.hex
        ),
        ModuleTypeIdentifierDto(
            ModuleTypeIdentifierId(1),
            ProjectId("myApp"),
            "KMP",
            "org.jetbrains.kotlin.jvm",
            4,
            ModuleColors.Purple.hex
        ),
    )

    override suspend fun fetchIdentifiers(projectId: ProjectId): Result<List<ModuleTypeIdentifierDto>, NetworkError> =
        Ok(identifiers.filter { it.projectId == projectId })

    override suspend fun create(
        projectId: ProjectId,
        typeName: String,
        plugin: String,
        order: Int,
        color: String
    ): Result<Unit, NetworkError> {
        identifiers.add(
            ModuleTypeIdentifierDto(
                ModuleTypeIdentifierId(nextId++),
                projectId,
                typeName,
                plugin,
                order,
                color
            )
        )
        return Ok(Unit)
    }

    override suspend fun update(
        id: ModuleTypeIdentifierId,
        typeName: String,
        plugin: String,
        order: Int,
        color: String
    ): Result<Unit, NetworkError> {
        val index = identifiers.indexOfFirst { it.id == id }
        if (index >= 0) {
            identifiers[index] =
                identifiers[index].copy(typeName = typeName, plugin = plugin, order = order, color = color)
        }
        return Ok(Unit)
    }

    override suspend fun delete(id: ModuleTypeIdentifierId): Result<Unit, NetworkError> {
        identifiers.removeAll { it.id == id }
        return Ok(Unit)
    }
}