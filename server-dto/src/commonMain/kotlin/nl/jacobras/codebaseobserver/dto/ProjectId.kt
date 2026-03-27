package nl.jacobras.codebaseobserver.dto

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class ProjectId(val value: String) {
    init {
        require(value.isNotBlank()) { "Project id cannot be blank" }
    }
}