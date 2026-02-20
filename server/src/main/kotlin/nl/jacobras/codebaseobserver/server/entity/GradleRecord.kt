package nl.jacobras.codebaseobserver.server.entity

import kotlinx.serialization.Serializable

@Serializable
data class GradleRecord(
    val projectId: String,
    val gitHash: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleTreeHeight: Int,
    val createdAt: String
)