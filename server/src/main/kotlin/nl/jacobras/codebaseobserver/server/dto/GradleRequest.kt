package nl.jacobras.codebaseobserver.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class GradleRequest(
    val gitHash: String,
    val gitDate: String,
    val moduleCount: Int
)