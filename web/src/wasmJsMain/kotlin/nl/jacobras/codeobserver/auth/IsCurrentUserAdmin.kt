package nl.jacobras.codeobserver.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import nl.jacobras.codeobserver.dto.UserRole
import org.koin.compose.koinInject

@Composable
internal fun isCurrentUserAdmin(): Boolean {
    val authState by koinInject<AuthRepository>().authState.collectAsState()
    return (authState as? AuthState.LoggedIn)?.user?.role == UserRole.Admin
}