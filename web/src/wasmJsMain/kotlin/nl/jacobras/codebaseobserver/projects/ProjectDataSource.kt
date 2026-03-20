package nl.jacobras.codebaseobserver.projects

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import nl.jacobras.codebaseobserver.data.NetworkError
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.dto.ProjectRequest

@Suppress("TooGenericExceptionCaught")
internal class ProjectDataSource(
    private val client: HttpClient
) {
    suspend fun fetch(): Result<List<ProjectDto>, NetworkError> {
        Logger.i("Fetching projects")
        return try {
            val projects = client.get("/projects").body<List<ProjectDto>>()
            Ok(projects)
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to fetch projects" }
            Err(NetworkError.UnknownError)
        }
    }

    suspend fun save(project: ProjectDto): Result<Unit, NetworkError> {
        Logger.i("Saving project: ${project.name}")
        return try {
            client.post("/projects") {
                contentType(ContentType.Application.Json)
                setBody(ProjectRequest(projectId = project.id, name = project.name))
            }
            Ok(Unit)
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to save project" }
            Err(NetworkError.UnknownError)
        }
    }

    suspend fun delete(id: String): Result<Unit, NetworkError> {
        Logger.i("Deleting project: $id")
        return try {
            client.delete("/projects/$id")
            Ok(Unit)
        } catch (e: Throwable) {
            Logger.e(e) { "Failed to delete project" }
            Err(NetworkError.UnknownError)
        }
    }
}