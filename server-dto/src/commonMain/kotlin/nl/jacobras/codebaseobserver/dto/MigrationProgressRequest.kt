package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MigrationProgressRequest(
    val migrationId: Int,
    val gitHash: String,
    val gitDate: Instant,
    val count: Int
)