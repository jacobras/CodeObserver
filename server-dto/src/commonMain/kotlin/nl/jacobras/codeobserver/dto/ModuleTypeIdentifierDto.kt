package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class ModuleTypeIdentifierDto(
    val id: ModuleTypeIdentifierId,
    val projectId: ProjectId,
    val typeName: String,
    val plugin: String,
    val order: Int,
    val color: String
)

@Serializable
@JvmInline
value class ModuleTypeIdentifierId(val value: Int) {
    init {
        require(value > 0) { "Module type identifier id must be positive, got: $value" }
    }
}