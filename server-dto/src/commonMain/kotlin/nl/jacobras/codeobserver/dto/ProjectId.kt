package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class ProjectId(val value: String) {
    init {
        val trimmed = value.trim()
        require(trimmed.isNotEmpty()) { "Project id cannot be blank" }
        require(value == trimmed) { "Project id cannot have leading or trailing whitespace" }
    }
}