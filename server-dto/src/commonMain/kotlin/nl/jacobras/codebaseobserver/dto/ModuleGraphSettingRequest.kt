package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModuleGraphSettingRequest(
    val projectId: String,
    val type: String,
    val data: String
)