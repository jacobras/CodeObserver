package nl.jacobras.codebaseobserver.cli.command.measure_gradle

import kotlinx.serialization.Serializable

@Serializable
data class GradleRequest(
    val gitHash: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleTreeHeight: Int
)