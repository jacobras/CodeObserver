package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

/**
 * Admin edit of a user. Null fields are left unchanged.
 */
@Serializable
data class UpdateUserRequest(
    val role: UserRole? = null,
    val password: String? = null
)