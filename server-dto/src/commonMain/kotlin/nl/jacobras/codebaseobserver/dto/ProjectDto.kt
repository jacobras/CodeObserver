package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: ProjectId,
    val name: String
)