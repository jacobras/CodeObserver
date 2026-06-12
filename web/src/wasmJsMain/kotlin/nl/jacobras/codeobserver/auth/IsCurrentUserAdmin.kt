package nl.jacobras.codeobserver.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import nl.jacobras.codeobserver.di.RepositoryLocator
import nl.jacobras.codeobserver.dto.UserRole

@Composable
internal fun isCurrentUserAdmin(): Boolean {
    val authState by RepositoryLocator.authRepository.authState.collectAsState()
    return (authState as? AuthState.LoggedIn)?.user?.role == UserRole.ADMIN
}