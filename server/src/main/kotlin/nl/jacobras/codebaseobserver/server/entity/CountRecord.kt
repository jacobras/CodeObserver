package nl.jacobras.codebaseobserver.server.entity

import kotlinx.serialization.Serializable

@Serializable
data class CountRecord(
    val gitHash: String,
    val gitDate: String,
    val fileCount: Int,
    val createdAt: String
)