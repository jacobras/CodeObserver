package nl.jacobras.codebaseobserver

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CountRecord(
    val gitHash: String,
    val gitDate: Instant,
    val fileCount: Int,
    val createdAt: String
)

@Serializable
data class CreateCountRequest(
    val gitHash: String,
    val gitDate: String,
    val fileCount: Int
)

@Serializable
data class UpdateCountRequest(
    val gitDate: String,
    val fileCount: Int
)
