package nl.jacobras.codebaseobserver.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGradleRequest(
    val gitDate: String,
    val moduleCount: Int
)