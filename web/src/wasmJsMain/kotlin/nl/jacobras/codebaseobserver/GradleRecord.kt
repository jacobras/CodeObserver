package nl.jacobras.codebaseobserver

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GradleRecord(
    val gitHash: String,
    val gitDate: Instant,
    val moduleCount: Int,
    val moduleHeight: Int,
    val createdAt: String
)

@Serializable
data class CreateGradleRequest(
    val gitHash: String,
    val gitDate: String,
    val moduleCount: Int,
    val moduleHeight: Int
)

@Serializable
data class UpdateGradleRequest(
    val gitDate: String,
    val moduleCount: Int,
    val moduleHeight: Int
)