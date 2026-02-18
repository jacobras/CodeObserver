package nl.jacobras.codebaseobserver.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCountRequest(
    val gitDate: String,
    val fileCount: Int
)