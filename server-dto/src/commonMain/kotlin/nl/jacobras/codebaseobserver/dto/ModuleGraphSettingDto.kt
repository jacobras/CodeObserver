package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ModuleGraphSettingDto(
    val id: Int,
    val createdAt: Instant,
    val projectId: String,
    val type: String,
    val data: String
)