package nl.jacobras.codebaseobserver.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactSizesDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactSizesRepository
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimesDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimesRepository
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektReportDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektReportRepository
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationProgressDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationProgressRepository
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationsDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationsRepository
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphSettingsDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphSettingsRepository
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypeIdentifiersRepository
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModulesRepository
import nl.jacobras.codebaseobserver.dashboard.trends.TrendsDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.trends.TrendsRepository
import nl.jacobras.codebaseobserver.projects.ProjectDataSourceImpl
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
        ModulesRepository(dataSource = ModuleGraphDataSourceImpl(httpClient))
    }
    val moduleGraphSettingsRepository: ModuleGraphSettingsRepository by lazy {
        ModuleGraphSettingsRepository(dataSource = ModuleGraphSettingsDataSourceImpl(httpClient))
    }
    val moduleTypeIdentifiersRepository: ModuleTypeIdentifiersRepository by lazy {
        ModuleTypeIdentifiersRepository(dataSource = ModuleTypeIdentifiersDataSourceImpl(httpClient))
    }
    val artifactSizesRepository: ArtifactSizesRepository by lazy {
        ArtifactSizesRepository(dataSource = ArtifactSizesDataSourceImpl(httpClient))
    }
    val buildTimesRepository: BuildTimesRepository by lazy {
        BuildTimesRepository(dataSource = BuildTimesDataSourceImpl(httpClient))
    }
    val migrationsRepository: MigrationsRepository by lazy {
        MigrationsRepository(dataSource = MigrationsDataSourceImpl(httpClient))
    }
    val migrationProgressRepository: MigrationProgressRepository by lazy {
        MigrationProgressRepository(dataSource = MigrationProgressDataSourceImpl(httpClient))
    }
    val trendsRepository: TrendsRepository by lazy {
        TrendsRepository(dataSource = TrendsDataSourceImpl(httpClient))
    }
    val detektReportRepository: DetektReportRepository by lazy {
        DetektReportRepository(dataSource = DetektReportDataSourceImpl(httpClient))
    }
    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(dataSource = ProjectDataSourceImpl(httpClient))
    }
}