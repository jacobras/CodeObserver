package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModuleTypeIdentifierUpdateRequest(
    val typeName: String,
    val plugin: String,
    val order: Int,
    val color: String
)