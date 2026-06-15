package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GradleDto(
    val longestPath: List<String> = emptyList(),
    val modules: List<GraphModuleDto> = emptyList(),
    val metrics: List<GradleMetricPointDto> = emptyList()
)

@Serializable
data class GradleMetricPointDto(
    val gitDate: Instant,
    val moduleCount: Int,
    val moduleTreeHeight: Int
)