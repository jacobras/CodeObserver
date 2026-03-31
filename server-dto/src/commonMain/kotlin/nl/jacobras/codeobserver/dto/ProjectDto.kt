package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: ProjectId,
    val name: String
)