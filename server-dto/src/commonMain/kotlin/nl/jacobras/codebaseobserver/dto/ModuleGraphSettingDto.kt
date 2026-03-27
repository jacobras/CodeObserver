package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@Serializable
data class ModuleGraphSettingDto(
    val id: ModuleGraphSettingId,
    val createdAt: Instant,
    val projectId: ProjectId,
    val type: String,
    val data: String
)

@Serializable
@JvmInline
value class ModuleGraphSettingId(val value: Int) {
    init {
        require(value > 0) { "Module graph setting id must be positive, got: $value" }
    }
}