package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class GraphModulesDto(
    val longestPath: List<String> = emptyList(),
    val modules: List<GraphModuleDto> = emptyList()
)