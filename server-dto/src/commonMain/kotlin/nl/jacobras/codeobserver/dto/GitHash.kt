package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class GitHash(val value: String) {
    init {
        require(value.isNotBlank()) { "Git hash cannot be blank" }
    }
}