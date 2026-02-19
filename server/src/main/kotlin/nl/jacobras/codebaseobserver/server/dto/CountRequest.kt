package nl.jacobras.codebaseobserver.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class CountRequest(
    val gitHash: String,
    val gitDate: String,
    val fileCount: Int
)