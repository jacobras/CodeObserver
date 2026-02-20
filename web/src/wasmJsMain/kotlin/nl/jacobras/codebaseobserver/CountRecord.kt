package nl.jacobras.codebaseobserver

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CountRecord(
    val projectId: String,
    val gitHash: String,
    val gitDate: Instant,
    val linesOfCode: Int,
    val createdAt: String
)

@Serializable
data class CreateCountRequest(
    val projectId: String,
    val gitHash: String,
    val gitDate: String,
    val linesOfCode: Int
)

@Serializable
data class UpdateCountRequest(
    val projectId: String,
    val gitDate: String,
    val linesOfCode: Int
)
