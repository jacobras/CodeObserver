package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class GraphVisualInfoDto(
    val modules: Map<String, List<String>> = emptyMap(),
    val config: List<GraphConfigDto> = emptyList(),
    val moduleColors: Map<String, String> = emptyMap()
)