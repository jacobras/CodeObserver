package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class MigrationUpdateRequest(
    val name: String,
    val description: String
)