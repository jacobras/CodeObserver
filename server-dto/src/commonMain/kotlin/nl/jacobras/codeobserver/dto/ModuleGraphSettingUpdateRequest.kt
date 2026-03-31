package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModuleGraphSettingUpdateRequest(
    val type: String,
    val data: String
)