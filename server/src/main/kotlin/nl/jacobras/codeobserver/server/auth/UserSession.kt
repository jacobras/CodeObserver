package nl.jacobras.codeobserver.server.auth

import kotlinx.serialization.Serializable

@Serializable
internal data class UserSession(val username: String)