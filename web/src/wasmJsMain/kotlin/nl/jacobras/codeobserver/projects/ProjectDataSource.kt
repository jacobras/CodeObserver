package nl.jacobras.codeobserver.projects

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import nl.jacobras.codeobserver.dto.ProjectDto
import nl.jacobras.codeobserver.dto.ProjectId
import nl.jacobras.codeobserver.dto.ProjectRequest
import nl.jacobras.codeobserver.util.data.NetworkError

internal interface ProjectDataSource {
    suspend fun fetch(): Result<List<ProjectDto>, NetworkError>
    suspend fun save(project: ProjectDto): Result<Unit, NetworkError>
    suspend fun delete(id: ProjectId): Result<Unit, NetworkError>
}

internal class ProjectDataSourceImpl(
    private val client: HttpClient
) : ProjectDataSource {
    override suspend fun fetch(): Result<List<ProjectDto>, NetworkError> {
        Logger.i("Fetching projects")
        return runSuspendCatching {
            client.get("/projects").body<List<ProjectDto>>()
        }.mapError {
            Logger.e(it) { "Failed to fetch projects" }
            NetworkError.UnknownError
        }
    }

    override suspend fun save(project: ProjectDto): Result<Unit, NetworkError> {
        Logger.i("Saving project: ${project.name}")
        return runSuspendCatching {
            client.post("/projects") {
                contentType(ContentType.Application.Json)
                setBody(ProjectRequest(projectId = project.id, name = project.name))
            }
            Unit
        }.mapError {
            Logger.e(it) { "Failed to save project" }
            NetworkError.UnknownError
        }
    }

    override suspend fun delete(id: ProjectId): Result<Unit, NetworkError> {
        Logger.i("Deleting project: ${id.value}")
        return runSuspendCatching {
            client.delete("/projects/${id.value}")
            Unit
        }.mapError {
            Logger.e(it) { "Failed to delete project" }
            NetworkError.UnknownError
        }
    }
}