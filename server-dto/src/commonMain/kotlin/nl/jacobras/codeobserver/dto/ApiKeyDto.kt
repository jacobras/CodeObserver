package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyDto(
    val key: String
)