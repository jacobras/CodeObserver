package nl.jacobras.codebaseobserver.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCountRequest(
    val projectId: String,
    val gitDate: String,
    val linesOfCode: Int
)