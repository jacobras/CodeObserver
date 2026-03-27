package nl.jacobras.codebaseobserver.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphSettingsDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphSettingsRepository
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypeIdentifiersRepository
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModulesDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModulesRepository
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactSizesDataSource
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactSizesRepository
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimesDataSource
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimesRepository
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektReportDataSource
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektReportRepository
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationProgressDataSource
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationProgressRepository
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationsDataSource
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationsRepository
import nl.jacobras.codebaseobserver.dashboard.trends.TrendsDataSource
import nl.jacobras.codebaseobserver.dashboard.trends.TrendsRepository
import nl.jacobras.codebaseobserver.projects.ProjectDataSource
import nl.jacobras.codebaseobserver.projects.ProjectRepository

/**
 * Placeholder until I've set up Koin.
 */
internal object RepositoryLocator {
    private val httpClient = HttpClient(Js) {
        defaultRequest {
            url("/")
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val modulesRepository: ModulesRepository by lazy {
        ModulesRepository(dataSource = ModulesDataSource(httpClient))
    }
    val moduleGraphSettingsRepository: ModuleGraphSettingsRepository by lazy {
        ModuleGraphSettingsRepository(dataSource = ModuleGraphSettingsDataSource(httpClient))
    }
    val moduleTypeIdentifiersRepository: ModuleTypeIdentifiersRepository by lazy {
        ModuleTypeIdentifiersRepository(dataSource = ModuleTypeIdentifiersDataSource(httpClient))
    }
    val artifactSizesRepository: ArtifactSizesRepository by lazy {
        ArtifactSizesRepository(dataSource = ArtifactSizesDataSource(httpClient))
    }
    val buildTimesRepository: BuildTimesRepository by lazy {
        BuildTimesRepository(dataSource = BuildTimesDataSource(httpClient))
    }
    val migrationsRepository: MigrationsRepository by lazy {
        MigrationsRepository(dataSource = MigrationsDataSource(httpClient))
    }
    val migrationProgressRepository: MigrationProgressRepository by lazy {
        MigrationProgressRepository(dataSource = MigrationProgressDataSource(httpClient))
    }
    val trendsRepository: TrendsRepository by lazy {
        TrendsRepository(dataSource = TrendsDataSource(httpClient))
    }
    val detektReportRepository: DetektReportRepository by lazy {
        DetektReportRepository(dataSource = DetektReportDataSource(httpClient))
    }
    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(dataSource = ProjectDataSource(httpClient))
    }
}