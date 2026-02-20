package nl.jacobras.codebaseobserver.cli.command.measure_code

import kotlinx.serialization.Serializable

@Serializable
data class CountRequest(
    val gitHash: String,
    val gitDate: String,
    val linesOfCode: Int
)