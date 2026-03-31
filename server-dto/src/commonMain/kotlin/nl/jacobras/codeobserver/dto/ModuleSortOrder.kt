package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
enum class ModuleSortOrder(val id: String, val displayName: String) {
    Alphabetical("alphabetical", "Alphabetical"),
    BetweennessCentrality("betweennessCentrality", "Betweenness Centrality");

    companion object {
        fun fromId(value: String): ModuleSortOrder? {
            return entries.find { it.id == value }
        }

        fun fromDisplayName(value: String): ModuleSortOrder {
            return entries.first { it.displayName == value }
        }
    }
}