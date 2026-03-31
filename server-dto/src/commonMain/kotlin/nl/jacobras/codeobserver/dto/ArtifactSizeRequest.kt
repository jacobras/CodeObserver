package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArtifactSizeRequest(
    val projectId: ProjectId,
    val name: String,
    val semVer: String,
    val size: Long
)