package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModuleTypeIdentifierDto(
    val id: Int,
    val projectId: String,
    val typeName: String,
    val plugin: String,
    val order: Int,
    val color: String
)