package nl.jacobras.codeobserver.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.jacobras.codeobserver.AppViewModel
import nl.jacobras.codeobserver.auth.AuthDataSource
import nl.jacobras.codeobserver.auth.AuthDataSourceImpl
import nl.jacobras.codeobserver.auth.AuthEvents
import nl.jacobras.codeobserver.auth.AuthRepository
import nl.jacobras.codeobserver.auth.LoginViewModel
import nl.jacobras.codeobserver.dashboard.artifacts.ArtifactChartsViewModel
import nl.jacobras.codeobserver.dashboard.artifacts.ArtifactSizesDataSource
import nl.jacobras.codeobserver.dashboard.artifacts.ArtifactSizesDataSourceImpl
import nl.jacobras.codeobserver.dashboard.artifacts.ArtifactSizesRepository
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesDataSource
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesDataSourceImpl
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesRepository
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesViewModel
import nl.jacobras.codeobserver.dashboard.detekt.DetektReportDataSource
import nl.jacobras.codeobserver.dashboard.detekt.DetektReportDataSourceImpl
import nl.jacobras.codeobserver.dashboard.detekt.DetektReportRepository
import nl.jacobras.codeobserver.dashboard.detekt.DetektTrendsViewModel
import nl.jacobras.codeobserver.dashboard.migrations.MigrationDetailViewModel
import nl.jacobras.codeobserver.dashboard.migrations.MigrationProgressDataSource
import nl.jacobras.codeobserver.dashboard.migrations.MigrationProgressDataSourceImpl
import nl.jacobras.codeobserver.dashboard.migrations.MigrationProgressRepository
import nl.jacobras.codeobserver.dashboard.migrations.MigrationsDataSource
import nl.jacobras.codeobserver.dashboard.migrations.MigrationsDataSourceImpl
import nl.jacobras.codeobserver.dashboard.migrations.MigrationsRepository
import nl.jacobras.codeobserver.dashboard.migrations.MigrationsViewModel
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphDataSourceImpl
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphRepository
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphSettingsDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphSettingsDataSourceImpl
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphSettingsRepository
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphViewModel
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleRulesViewModel
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSourceImpl
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleTypeIdentifiersRepository
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleTypesViewModel
import nl.jacobras.codeobserver.dashboard.trends.TrendsDataSource
import nl.jacobras.codeobserver.dashboard.trends.TrendsDataSourceImpl
import nl.jacobras.codeobserver.dashboard.trends.TrendsRepository
import nl.jacobras.codeobserver.dashboard.trends.TrendsViewModel
import nl.jacobras.codeobserver.di.demo.DemoArtifactSizesDataSource
import nl.jacobras.codeobserver.di.demo.DemoAuthDataSource
import nl.jacobras.codeobserver.di.demo.DemoBuildTimesDataSource
import nl.jacobras.codeobserver.di.demo.DemoDetektReportDataSource
import nl.jacobras.codeobserver.di.demo.DemoMigrationProgressDataSource
import nl.jacobras.codeobserver.di.demo.DemoMigrationsDataSource
import nl.jacobras.codeobserver.di.demo.DemoModuleGraphDataSource
import nl.jacobras.codeobserver.di.demo.DemoModuleGraphSettingsDataSource
import nl.jacobras.codeobserver.di.demo.DemoModuleTypeIdentifiersDataSource
import nl.jacobras.codeobserver.di.demo.DemoProjectDataSource
import nl.jacobras.codeobserver.di.demo.DemoTrendsDataSource
import nl.jacobras.codeobserver.di.demo.DemoUsersDataSource
import nl.jacobras.codeobserver.projects.ProjectDataSource
import nl.jacobras.codeobserver.projects.ProjectDataSourceImpl
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.settings.ChangePasswordViewModel
import nl.jacobras.codeobserver.settings.SettingsScreenViewModel
import nl.jacobras.codeobserver.users.UsersDataSource
import nl.jacobras.codeobserver.users.UsersDataSourceImpl
import nl.jacobras.codeobserver.users.UsersRepository
import nl.jacobras.codeobserver.users.UsersScreenViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin definitions for the web app.
 *
 * App-level singletons (HTTP transport, data sources and [AuthRepository]) survive
 * across login/logout. Everything that holds per-user data lives in [SessionScope] and
 * is destroyed when the session scope closes on logout.
 */
internal fun appModule(isDemo: Boolean): Module = module {
    dataSources(isDemo)

    single { AuthRepository(get()) }
    viewModel { LoginViewModel(get()) }

    scope<SessionScope> {
        scoped { ProjectRepository(get()) }
        scoped { UsersRepository(get()) }
        scoped { ArtifactSizesRepository(get()) }
        scoped { BuildTimesRepository(get()) }
        scoped { DetektReportRepository(get()) }
        scoped { MigrationsRepository(get()) }
        scoped { MigrationProgressRepository(get()) }
        scoped { TrendsRepository(get()) }
        scoped { ModuleGraphRepository(get()) }
        scoped { ModuleGraphSettingsRepository(get()) }
        scoped { ModuleTypeIdentifiersRepository(get()) }

        viewModel { AppViewModel(get()) }
        viewModel { ArtifactChartsViewModel(get(), get()) }
        viewModel { BuildTimesViewModel(get(), get()) }
        viewModel { TrendsViewModel(get(), get()) }
        viewModel { DetektTrendsViewModel(get(), get()) }
        viewModel { MigrationsViewModel(get(), get()) }
        viewModel { MigrationDetailViewModel(get()) }
        viewModel { ModuleGraphViewModel(get(), get()) }
        viewModel { ModuleRulesViewModel(get(), get()) }
        viewModel { ModuleTypesViewModel(get(), get()) }
        viewModel { SettingsScreenViewModel(get()) }
        viewModel { UsersScreenViewModel(get()) }
        viewModel { ChangePasswordViewModel(get()) }
    }
}

private fun Module.dataSources(isDemo: Boolean) {
    if (isDemo) {
        single<AuthDataSource> { DemoAuthDataSource() }
        single<UsersDataSource> { DemoUsersDataSource() }
        single<ArtifactSizesDataSource> { DemoArtifactSizesDataSource() }
        single<BuildTimesDataSource> { DemoBuildTimesDataSource() }
        single<DetektReportDataSource> { DemoDetektReportDataSource() }
        single<MigrationProgressDataSource> { DemoMigrationProgressDataSource() }
        single<MigrationsDataSource> { DemoMigrationsDataSource() }
        single<ModuleGraphDataSource> { DemoModuleGraphDataSource() }
        single<ModuleGraphSettingsDataSource> { DemoModuleGraphSettingsDataSource() }
        single<ModuleTypeIdentifiersDataSource> { DemoModuleTypeIdentifiersDataSource() }
        single<ProjectDataSource> { DemoProjectDataSource() }
        single<TrendsDataSource> { DemoTrendsDataSource() }
    } else {
        single { createHttpClient() }
        single<AuthDataSource> { AuthDataSourceImpl(get()) }
        single<UsersDataSource> { UsersDataSourceImpl(get()) }
        single<ArtifactSizesDataSource> { ArtifactSizesDataSourceImpl(get()) }
        single<BuildTimesDataSource> { BuildTimesDataSourceImpl(get()) }
        single<DetektReportDataSource> { DetektReportDataSourceImpl(get()) }
        single<MigrationProgressDataSource> { MigrationProgressDataSourceImpl(get()) }
        single<MigrationsDataSource> { MigrationsDataSourceImpl(get()) }
        single<ModuleGraphDataSource> { ModuleGraphDataSourceImpl(get()) }
        single<ModuleGraphSettingsDataSource> { ModuleGraphSettingsDataSourceImpl(get()) }
        single<ModuleTypeIdentifiersDataSource> { ModuleTypeIdentifiersDataSourceImpl(get()) }
        single<ProjectDataSource> { ProjectDataSourceImpl(get()) }
        single<TrendsDataSource> { TrendsDataSourceImpl(get()) }
    }
}

private fun createHttpClient(): HttpClient = HttpClient(Js) {
    defaultRequest {
        url("/")
    }
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            val status = (cause as? ResponseException)?.response?.status
            val path = request.url.encodedPath
            if (status == HttpStatusCode.Unauthorized && !path.endsWith("/login") && !path.endsWith("/logout")) {
                AuthEvents.onUnauthorized()
            }
        }
    }
}