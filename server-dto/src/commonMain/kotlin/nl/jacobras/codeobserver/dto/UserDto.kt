package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val username: String,
    val role: UserRole
)