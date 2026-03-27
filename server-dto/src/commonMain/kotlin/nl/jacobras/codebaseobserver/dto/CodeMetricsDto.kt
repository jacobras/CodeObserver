package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CodeMetricsDto(
    val projectId: ProjectId,
    val createdAt: Instant,
    val gitHash: GitHash,
    val gitDate: Instant,
    val linesOfCode: Int,
    val moduleCount: Int,
    val moduleTreeHeight: Int
)