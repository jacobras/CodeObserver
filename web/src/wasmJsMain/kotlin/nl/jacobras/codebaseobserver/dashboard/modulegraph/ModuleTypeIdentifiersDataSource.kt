package nl.jacobras.codebaseobserver.dashboard.modulegraph

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierDto
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierId
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierRequest
import nl.jacobras.codebaseobserver.dto.ModuleTypeIdentifierUpdateRequest
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class ModuleTypeIdentifiersDataSource(
    private val client: HttpClient
) {
    suspend fun fetchIdentifiers(projectId: ProjectId): Result<List<ModuleTypeIdentifierDto>, NetworkError> {
        Logger.i("Fetching module type identifiers for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/moduleTypeIdentifiers") {
                url { parameters.append("projectId", projectId.value) }
            }.body<List<ModuleTypeIdentifierDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch module type identifiers for project ${projectId.value}" }
            NetworkError.UnknownError
        }
    }

    suspend fun create(
        projectId: ProjectId,
        typeName: String,
        plugin: String,
        order: Int,
        color: String
    ): Result<Unit, NetworkError> {
        Logger.i("Creating module type identifier for project ${projectId.value}")
        return runSuspendCatching {
            client.post("/moduleTypeIdentifiers") {
                contentType(ContentType.Application.Json)
                setBody(
                    ModuleTypeIdentifierRequest(
                        projectId = projectId,
                        typeName = typeName,
                        plugin = plugin,
                        order = order,
                        color = color
                    )
                )
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to create module type identifier" }
            NetworkError.UnknownError
        }
    }

    suspend fun update(
        id: ModuleTypeIdentifierId,
        typeName: String,
        plugin: String,
        order: Int,
        color: String
    ): Result<Unit, NetworkError> {
        Logger.i("Updating module type identifier ${id.value}")
        return runSuspendCatching {
            client.patch("/moduleTypeIdentifiers/${id.value}") {
                contentType(ContentType.Application.Json)
                setBody(
                    ModuleTypeIdentifierUpdateRequest(
                        typeName = typeName,
                        plugin = plugin,
                        order = order,
                        color = color
                    )
                )
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to update module type identifier" }
            NetworkError.UnknownError
        }
    }

    suspend fun delete(id: ModuleTypeIdentifierId): Result<Unit, NetworkError> {
        Logger.i("Deleting module type identifier ${id.value}")
        return runSuspendCatching {
            client.delete("/moduleTypeIdentifiers/${id.value}")
            Logger.i("Module type identifier ${id.value} deleted")
        }.mapError {
            Logger.e(it) { "Failed to delete module type identifier" }
            NetworkError.UnknownError
        }
    }
}