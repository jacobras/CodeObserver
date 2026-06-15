package nl.jacobras.codeobserver.di

/**
 * Koin scope that lives for the duration of a logged-in user session.
 *
 * Everything reachable only after login (repositories holding per-user data and the
 * screens' view models) is registered in this scope. The scope is opened when the
 * authenticated UI enters composition and closed when the user logs out (or a 401
 * forces a logout), so no data leaks from one user to the next.
 */
internal class SessionScope