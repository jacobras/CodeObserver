package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MigrationProgressDto(
    val migrationId: Int,
    val gitHash: GitHash,
    val gitDate: Instant,
    val count: Int
)