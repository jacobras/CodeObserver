package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class MigrationRequest(
    val projectId: String,
    val type: String,
    val rule: String
)