package nl.jacobras.codebaseobserver

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GradleRecord(
    val projectId: String,
    val gitHash: String,
    val gitDate: Instant,
    val moduleCount: Int,
    val moduleTreeHeight: Int,
    val createdAt: String
)

@Serializable
data class CreateGradleRequest(
    val projectId: String,
    val gitHash: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleTreeHeight: Int
)

@Serializable
data class UpdateGradleRequest(
    val projectId: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleTreeHeight: Int
)