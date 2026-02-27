package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

/**
 * @property name The name of the module, e.g., "feature:profile".
 * @property score The score of the module, e.g., the betweenness centrality score, or 0 if nothing loaded.
 */
@Serializable
data class GraphModuleDto(
    val name: String,
    val score: Int
)