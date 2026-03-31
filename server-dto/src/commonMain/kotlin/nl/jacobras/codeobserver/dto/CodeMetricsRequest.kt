package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CodeMetricsRequest(
    override val projectId: ProjectId,
    override val gitHash: GitHash,
    override val gitDate: Instant,
    val linesOfCode: Int
) : ProjectAndGitInfo