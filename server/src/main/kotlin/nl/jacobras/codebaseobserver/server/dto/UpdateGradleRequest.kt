package nl.jacobras.codebaseobserver.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGradleRequest(
    val projectId: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleTreeHeight: Int
)