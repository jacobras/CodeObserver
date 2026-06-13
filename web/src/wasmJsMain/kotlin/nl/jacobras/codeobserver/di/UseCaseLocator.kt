package nl.jacobras.codeobserver.di

import nl.jacobras.codeobserver.auth.LogoutUseCase

internal object UseCaseLocator {
    val logoutUseCase = LogoutUseCase()
}