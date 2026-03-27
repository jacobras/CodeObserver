package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@Serializable
data class DetektMetricDto(
    val id: ReportId,
    val projectId: ProjectId,
    val gitHash: GitHash,
    val gitDate: Instant,
    val findings: Int,
    val smellsPer1000: Int
)

@Serializable
@JvmInline
value class ReportId(val value: Int) {
    init {
        require(value > 0) { "ReportId must be positive, got: $value" }
    }
}