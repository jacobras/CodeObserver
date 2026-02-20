package nl.jacobras.codebaseobserver.cli

import kotlinx.serialization.Serializable

@Serializable
data class CountRequest(
    val gitHash: String,
    val gitDate: String,
    val linesOfCode: Int
)