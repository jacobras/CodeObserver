package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DetektReportRequest(
    val projectId: ProjectId,
    val gitHash: GitHash,
    val gitDate: Instant,
    val findings: Int,
    val smellsPer1000: Int,
    val htmlReport: String
)