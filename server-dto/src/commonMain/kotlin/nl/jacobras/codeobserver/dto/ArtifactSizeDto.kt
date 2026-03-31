package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ArtifactSizeDto(
    val projectId: ProjectId,
    val createdAt: Instant,
    val name: String,
    val semVer: String,
    val size: Long
)