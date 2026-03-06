package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class MigrationUpdateRequest(
    val name: String,
    val description: String
)