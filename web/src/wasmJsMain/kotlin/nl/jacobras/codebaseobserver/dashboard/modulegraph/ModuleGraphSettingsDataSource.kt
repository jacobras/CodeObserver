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
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingDto
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingId
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingRequest
import nl.jacobras.codebaseobserver.dto.ModuleGraphSettingUpdateRequest
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class ModuleGraphSettingsDataSource(
    private val client: HttpClient
) {
    suspend fun fetchSettings(projectId: ProjectId): Result<List<ModuleGraphSettingDto>, NetworkError> {
        Logger.i("Fetching module graph settings for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/moduleGraphSettings") {
                url { parameters.append("projectId", projectId.value) }
            }.body<List<ModuleGraphSettingDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch module graph settings" }
            NetworkError.UnknownError
        }
    }

    suspend fun create(projectId: ProjectId, type: String, data: String): Result<Unit, NetworkError> {
        Logger.i("Creating module graph setting for project ${projectId.value}")
        return runSuspendCatching {
            client.post("/moduleGraphSettings") {
                contentType(ContentType.Application.Json)
                setBody(ModuleGraphSettingRequest(projectId = projectId, type = type, data = data))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to create module graph setting" }
            NetworkError.UnknownError
        }
    }

    suspend fun update(id: ModuleGraphSettingId, type: String, data: String): Result<Unit, NetworkError> {
        Logger.i("Updating module graph setting ${id.value}")
        return runSuspendCatching {
            client.patch("/moduleGraphSettings/${id.value}") {
                contentType(ContentType.Application.Json)
                setBody(ModuleGraphSettingUpdateRequest(type = type, data = data))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to update module graph setting" }
            NetworkError.UnknownError
        }
    }

    suspend fun delete(id: ModuleGraphSettingId): Result<Unit, NetworkError> {
        Logger.i("Deleting module graph setting ${id.value}")
        return runSuspendCatching {
            client.delete("/moduleGraphSettings/${id.value}")
            Logger.i("Module graph setting ${id.value} deleted")
        }.mapError {
            Logger.e(it) { "Failed to delete module graph setting" }
            NetworkError.UnknownError
        }
    }
}