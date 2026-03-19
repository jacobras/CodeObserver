package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BuildTimeRequest(
    val projectId: String,
    val buildName: String,
    val gitHash: String,
    val gitDate: Instant,
    val timeSeconds: Int
)