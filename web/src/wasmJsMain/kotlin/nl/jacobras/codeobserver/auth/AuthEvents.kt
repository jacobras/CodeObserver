package nl.jacobras.codeobserver.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Global hook for 401 responses, so the HTTP client can notify
 * the auth state changing to unauthorized without depending on the repo (which it can't access).
 *
 * This can be nicer, but it works.
 */
internal object AuthEvents {
    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized: SharedFlow<Unit> = _unauthorized

    fun onUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }
}