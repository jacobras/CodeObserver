package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModuleGraphSettingRequest(
    val projectId: ProjectId,
    val type: String,
    val data: String
)