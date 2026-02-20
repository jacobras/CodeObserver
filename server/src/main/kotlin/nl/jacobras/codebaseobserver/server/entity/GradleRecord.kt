package nl.jacobras.codebaseobserver.server.entity

import kotlinx.serialization.Serializable

@Serializable
data class GradleRecord(
    val gitHash: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleHeight: Int,
    val createdAt: String
)