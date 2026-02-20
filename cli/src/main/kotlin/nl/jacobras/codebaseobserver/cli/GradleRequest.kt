package nl.jacobras.codebaseobserver.cli

import kotlinx.serialization.Serializable

@Serializable
data class GradleRequest(
    val gitHash: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleHeight: Int
)