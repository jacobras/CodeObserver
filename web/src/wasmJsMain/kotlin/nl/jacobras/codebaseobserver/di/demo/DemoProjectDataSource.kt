package nl.jacobras.codebaseobserver.di.demo

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import nl.jacobras.codebaseobserver.dto.ProjectDto
import nl.jacobras.codebaseobserver.dto.ProjectId
import nl.jacobras.codebaseobserver.projects.ProjectDataSource
import nl.jacobras.codebaseobserver.util.data.NetworkError

internal class DemoProjectDataSource : ProjectDataSource {
    private val projects = mutableListOf(
        ProjectDto(id = ProjectId("myApp"), name = "My App")
    )

    override suspend fun fetch(): Result<List<ProjectDto>, NetworkError> = Ok(projects.toList())

    override suspend fun save(project: ProjectDto): Result<Unit, NetworkError> {
        val index = projects.indexOfFirst { it.id == project.id }
        if (index >= 0) {
            projects[index] = project
        } else {
            projects.add(project)
        }
        return Ok(Unit)
    }

    override suspend fun delete(id: ProjectId): Result<Unit, NetworkError> {
        projects.removeAll { it.id == id }
        return Ok(Unit)
    }
}