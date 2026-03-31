package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectRequest(
    val projectId: ProjectId,
    val name: String
)