package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectRequest(
    val projectId: String,
    val name: String
)