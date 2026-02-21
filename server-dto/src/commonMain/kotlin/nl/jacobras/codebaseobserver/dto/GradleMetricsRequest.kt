package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GradleMetricsRequest(
    val projectId: String,
    val gitHash: String,
    val gitDate: Instant,
    val moduleCount: Int,
    val moduleTreeHeight: Int
)