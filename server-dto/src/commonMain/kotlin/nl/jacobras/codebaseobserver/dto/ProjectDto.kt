package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val projectId: String,
    val name: String
)
