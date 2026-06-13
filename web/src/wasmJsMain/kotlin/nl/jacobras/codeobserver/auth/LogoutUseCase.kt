package nl.jacobras.codeobserver.auth

import nl.jacobras.codeobserver.di.RepositoryLocator

internal class LogoutUseCase {

    suspend operator fun invoke() {
        RepositoryLocator.authRepository.logout()
        RepositoryLocator.usersRepository.clearCache()
    }
}