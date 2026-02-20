package nl.jacobras.codebaseobserver.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class GradleRequest(
    val projectId: String,
    val gitHash: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleTreeHeight: Int
)