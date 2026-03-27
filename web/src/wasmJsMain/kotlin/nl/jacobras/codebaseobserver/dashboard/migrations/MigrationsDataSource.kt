package nl.jacobras.codebaseobserver.dashboard.migrations

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
import nl.jacobras.codebaseobserver.dto.MigrationDto
import nl.jacobras.codebaseobserver.dto.MigrationRequest
import nl.jacobras.codebaseobserver.dto.MigrationUpdateRequest
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class MigrationsDataSource(
    private val client: HttpClient
) {
    suspend fun fetchMigrations(projectId: String): Result<List<MigrationDto>, NetworkError> {
        Logger.i("Fetching migrations for project $projectId")
        return runSuspendCatching {
            client.get("/migrations") {
                url { parameters.append("projectId", projectId) }
            }.body<List<MigrationDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch migrations" }
            NetworkError.UnknownError
        }
    }

    suspend fun create(
        projectId: String,
        name: String,
        description: String,
        type: String,
        rule: String
    ): Result<Unit, NetworkError> {
        Logger.i("Creating migration for project $projectId")
        return runSuspendCatching {
            client.post("/migrations") {
                contentType(ContentType.Application.Json)
                setBody(MigrationRequest(projectId = projectId, name = name, description = description, type = type, rule = rule))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to create migration" }
            NetworkError.UnknownError
        }
    }

    suspend fun update(id: Int, name: String, description: String): Result<Unit, NetworkError> {
        Logger.i("Updating migration $id")
        return runSuspendCatching {
            client.patch("/migrations/$id") {
                contentType(ContentType.Application.Json)
                setBody(MigrationUpdateRequest(name = name, description = description))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to update migration" }
            NetworkError.UnknownError
        }
    }

    suspend fun delete(id: Int): Result<Unit, NetworkError> {
        Logger.i("Deleting migration $id")
        return runSuspendCatching {
            client.delete("/migrations/$id")
            Logger.i("Migration $id deleted")
        }.mapError {
            Logger.e(it) { "Failed to delete migration" }
            NetworkError.UnknownError
        }
    }
}