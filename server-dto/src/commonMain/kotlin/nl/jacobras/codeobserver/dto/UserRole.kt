package nl.jacobras.codeobserver.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {

    @SerialName("Admin")
    Admin,

    @SerialName("Developer")
    Developer
}