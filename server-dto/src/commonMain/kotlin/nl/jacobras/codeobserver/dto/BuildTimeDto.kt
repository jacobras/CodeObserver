package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BuildTimeDto(
    val projectId: ProjectId,
    val buildName: String,
    val gitHash: GitHash,
    val gitDate: Instant,
    val timeSeconds: Int
)