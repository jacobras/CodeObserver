package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@Serializable
data class MigrationDto(
    val id: MigrationId,
    val createdAt: Instant,
    val name: String,
    val description: String,
    val projectId: ProjectId,
    val type: String,
    val rule: String
)

@Serializable
@JvmInline
value class MigrationId(val value: Int) {
    init {
        require(value > 0) { "Migration id must be positive, got: $value" }
    }
}