package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    ADMIN,
    DEVELOPER
}