package nl.jacobras.codebaseobserver.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactSizesDataSource
import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactSizesDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimesDataSource
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimesDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektReportDataSource
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektReportDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationProgressDataSource
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationProgressDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationsDataSource
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationsDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphSettingsDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphSettingsDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSourceImpl
import nl.jacobras.codebaseobserver.dashboard.trends.TrendsDataSource
import nl.jacobras.codebaseobserver.dashboard.trends.TrendsDataSourceImpl
import nl.jacobras.codebaseobserver.projects.ProjectDataSource
import nl.jacobras.codebaseobserver.projects.ProjectDataSourceImpl

internal interface DataSourceLocator {
    val artifactSizesDataSource: ArtifactSizesDataSource
    val buildTimesDataSource: BuildTimesDataSource
    val detektReportDataSource: DetektReportDataSource
    val migrationProgressDataSource: MigrationProgressDataSource
    val migrationsDataSource: MigrationsDataSource
    val moduleGraphDataSource: ModuleGraphDataSource
    val moduleGraphSettingsDataSource: ModuleGraphSettingsDataSource
    val moduleTypeIdentifiersDataSource: ModuleTypeIdentifiersDataSource
    val projectDataSource: ProjectDataSource
    val trendsDataSource: TrendsDataSource
}

internal object DatabaseDataSourceLocator : DataSourceLocator {
    private val httpClient = HttpClient(Js) {
        defaultRequest {
            url("/")
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override val artifactSizesDataSource = ArtifactSizesDataSourceImpl(httpClient)
    override val buildTimesDataSource = BuildTimesDataSourceImpl(httpClient)
    override val detektReportDataSource = DetektReportDataSourceImpl(httpClient)
    override val migrationProgressDataSource = MigrationProgressDataSourceImpl(httpClient)
    override val migrationsDataSource = MigrationsDataSourceImpl(httpClient)
    override val moduleGraphDataSource = ModuleGraphDataSourceImpl(httpClient)
    override val moduleGraphSettingsDataSource = ModuleGraphSettingsDataSourceImpl(httpClient)
    override val moduleTypeIdentifiersDataSource = ModuleTypeIdentifiersDataSourceImpl(httpClient)
    override val projectDataSource = ProjectDataSourceImpl(httpClient)
    override val trendsDataSource = TrendsDataSourceImpl(httpClient)
}