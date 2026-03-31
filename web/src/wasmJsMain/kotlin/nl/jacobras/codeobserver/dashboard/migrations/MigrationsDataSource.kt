package nl.jacobras.codeobserver.dashboard.migrations

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
import nl.jacobras.codeobserver.dto.MigrationDto
import nl.jacobras.codeobserver.dto.MigrationId
import nl.jacobras.codeobserver.dto.MigrationRequest
import nl.jacobras.codeobserver.dto.MigrationUpdateRequest
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.util.data.NetworkError

internal interface MigrationsDataSource {
    suspend fun fetchMigrations(projectId: ProjectId): Result<List<MigrationDto>, NetworkError>
    suspend fun create(
        projectId: ProjectId,
        name: String,
        description: String,
        type: String,
        rule: String
    ): Result<Unit, NetworkError>
    suspend fun update(id: MigrationId, name: String, description: String): Result<Unit, NetworkError>
    suspend fun delete(id: MigrationId): Result<Unit, NetworkError>
}

internal class MigrationsDataSourceImpl(
    private val client: HttpClient
) : MigrationsDataSource {
    override suspend fun fetchMigrations(projectId: ProjectId): Result<List<MigrationDto>, NetworkError> {
        Logger.i("Fetching migrations for project ${projectId.value}")
        return runSuspendCatching {
            client.get("/migrations") {
                url { parameters.append("projectId", projectId.value) }
            }.body<List<MigrationDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch migrations" }
            NetworkError.UnknownError
        }
    }

    override suspend fun create(
        projectId: ProjectId,
        name: String,
        description: String,
        type: String,
        rule: String
    ): Result<Unit, NetworkError> {
        Logger.i("Creating migration for project ${projectId.value}")
        return runSuspendCatching {
            client.post("/migrations") {
                contentType(ContentType.Application.Json)
                setBody(
                    MigrationRequest(
                        projectId = projectId,
                        name = name,
                        description = description,
                        type = type,
                        rule = rule
                    )
                )
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to create migration" }
            NetworkError.UnknownError
        }
    }

    override suspend fun update(id: MigrationId, name: String, description: String): Result<Unit, NetworkError> {
        Logger.i("Updating migration ${id.value}")
        return runSuspendCatching {
            client.patch("/migrations/${id.value}") {
                contentType(ContentType.Application.Json)
                setBody(MigrationUpdateRequest(name = name, description = description))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to update migration" }
            NetworkError.UnknownError
        }
    }

    override suspend fun delete(id: MigrationId): Result<Unit, NetworkError> {
        Logger.i("Deleting migration ${id.value}")
        return runSuspendCatching {
            client.delete("/migrations/${id.value}")
            Logger.i("Migration ${id.value} deleted")
        }.mapError {
            Logger.e(it) { "Failed to delete migration" }
            NetworkError.UnknownError
        }
    }
}