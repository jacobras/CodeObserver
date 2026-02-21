package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CodeMetricsRequest(
    override val projectId: String,
    override val gitHash: String,
    override val gitDate: Instant,
    val linesOfCode: Int
) : ProjectAndGitInfo