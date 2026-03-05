package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MigrationDto(
    val id: Int,
    val createdAt: Instant,
    val name: String,
    val projectId: String,
    val type: String,
    val rule: String
)