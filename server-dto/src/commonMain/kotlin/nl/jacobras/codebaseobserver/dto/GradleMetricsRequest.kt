package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GradleMetricsRequest(
    override val projectId: String,
    override val gitHash: String,
    override val gitDate: Instant,
    val moduleCount: Int,
    val longestPath: List<String>,
    val graph: Map<String, List<String>>,
    val moduleDetails: String = ""
) : ProjectAndGitInfo