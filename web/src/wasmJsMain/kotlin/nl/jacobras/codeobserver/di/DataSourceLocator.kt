package nl.jacobras.codeobserver.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.jacobras.codeobserver.dashboard.artifacts.ArtifactSizesDataSource
import nl.jacobras.codeobserver.dashboard.artifacts.ArtifactSizesDataSourceImpl
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesDataSource
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesDataSourceImpl
import nl.jacobras.codeobserver.dashboard.detekt.DetektReportDataSource
import nl.jacobras.codeobserver.dashboard.detekt.DetektReportDataSourceImpl
import nl.jacobras.codeobserver.dashboard.migrations.MigrationProgressDataSource
import nl.jacobras.codeobserver.dashboard.migrations.MigrationProgressDataSourceImpl
import nl.jacobras.codeobserver.dashboard.migrations.MigrationsDataSource
import nl.jacobras.codeobserver.dashboard.migrations.MigrationsDataSourceImpl
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphDataSourceImpl
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphSettingsDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphSettingsDataSourceImpl
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSourceImpl
import nl.jacobras.codeobserver.dashboard.trends.TrendsDataSource
import nl.jacobras.codeobserver.dashboard.trends.TrendsDataSourceImpl
import nl.jacobras.codeobserver.projects.ProjectDataSource
import nl.jacobras.codeobserver.projects.ProjectDataSourceImpl

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
        expectSuccess = true
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