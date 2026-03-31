package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModuleTypeIdentifierRequest(
    val projectId: ProjectId,
    val typeName: String,
    val plugin: String,
    val order: Int,
    val color: String
)