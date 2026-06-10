package nl.jacobras.codeobserver.server.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.response.respond

internal class ApiKeyAuthenticationProvider(
    config: Config
) : AuthenticationProvider(config) {

    internal class Config(name: String?) : AuthenticationProvider.Config(name) {
        lateinit var validate: (String) -> Boolean
    }

    private val validate = config.validate

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val key = context.call.request.headers[API_KEY_HEADER]
        if (key != null && validate(key)) {
            context.principal(ApiKeyPrincipal)
        } else {
            context.challenge("ApiKey", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Authentication required"))
                challenge.complete()
            }
        }
    }
}

internal fun AuthenticationConfig.apiKey(
    name: String,
    configure: ApiKeyAuthenticationProvider.Config.() -> Unit
) {
    register(ApiKeyAuthenticationProvider(ApiKeyAuthenticationProvider.Config(name).apply(configure)))
}

private const val API_KEY_HEADER = "X-Api-Key"