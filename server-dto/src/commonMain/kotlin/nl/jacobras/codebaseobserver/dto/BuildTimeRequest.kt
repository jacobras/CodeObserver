package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BuildTimeRequest(
    val projectId: ProjectId,
    val buildName: String,
    val gitHash: GitHash,
    val gitDate: Instant,
    val timeSeconds: Int
)