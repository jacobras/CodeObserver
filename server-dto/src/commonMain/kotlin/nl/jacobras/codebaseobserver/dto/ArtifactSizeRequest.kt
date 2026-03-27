package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArtifactSizeRequest(
    val projectId: ProjectId,
    val name: String,
    val semVer: String,
    val size: Long
)