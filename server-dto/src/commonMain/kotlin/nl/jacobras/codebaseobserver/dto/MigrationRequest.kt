package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class MigrationRequest(
    val projectId: ProjectId,
    val name: String,
    val description: String,
    val type: String,
    val rule: String
)