package nl.jacobras.codebaseobserver.server.entity

import kotlinx.serialization.Serializable

@Serializable
data class CountRecord(
    val projectId: String,
    val gitHash: String,
    val gitDate: String,
    val linesOfCode: Int,
    val createdAt: String
)