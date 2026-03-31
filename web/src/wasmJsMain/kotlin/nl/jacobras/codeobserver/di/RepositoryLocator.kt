package nl.jacobras.codeobserver.di

import nl.jacobras.codeobserver.dashboard.artifacts.ArtifactSizesRepository
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesRepository
import nl.jacobras.codeobserver.dashboard.detekt.DetektReportRepository
import nl.jacobras.codeobserver.dashboard.migrations.MigrationProgressRepository
import nl.jacobras.codeobserver.dashboard.migrations.MigrationsRepository
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphSettingsRepository
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleTypeIdentifiersRepository
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphRepository
import nl.jacobras.codeobserver.dashboard.trends.TrendsRepository
import nl.jacobras.codeobserver.di.demo.DemoDataSourceLocator
import nl.jacobras.codeobserver.projects.ProjectRepository
import nl.jacobras.codeobserver.web.BuildConfig

/**
 * Placeholder until I've set up Koin.
 */
internal object RepositoryLocator {
    val dataSourceLocator = if (BuildConfig.IS_DEMO) DemoDataSourceLocator else DatabaseDataSourceLocator

    val modulesRepository: ModuleGraphRepository by lazy {
        ModuleGraphRepository(dataSource = dataSourceLocator.moduleGraphDataSource)
    }
    val moduleGraphSettingsRepository: ModuleGraphSettingsRepository by lazy {
        ModuleGraphSettingsRepository(dataSource = dataSourceLocator.moduleGraphSettingsDataSource)
    }
    val moduleTypeIdentifiersRepository: ModuleTypeIdentifiersRepository by lazy {
        ModuleTypeIdentifiersRepository(dataSource = dataSourceLocator.moduleTypeIdentifiersDataSource)
    }
    val artifactSizesRepository: ArtifactSizesRepository by lazy {
        ArtifactSizesRepository(dataSource = dataSourceLocator.artifactSizesDataSource)
    }
    val buildTimesRepository: BuildTimesRepository by lazy {
        BuildTimesRepository(dataSource = dataSourceLocator.buildTimesDataSource)
    }
    val migrationsRepository: MigrationsRepository by lazy {
        MigrationsRepository(dataSource = dataSourceLocator.migrationsDataSource)
    }
    val migrationProgressRepository: MigrationProgressRepository by lazy {
        MigrationProgressRepository(dataSource = dataSourceLocator.migrationProgressDataSource)
    }
    val trendsRepository: TrendsRepository by lazy {
        TrendsRepository(dataSource = dataSourceLocator.trendsDataSource)
    }
    val detektReportRepository: DetektReportRepository by lazy {
        DetektReportRepository(dataSource = dataSourceLocator.detektReportDataSource)
    }
    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(dataSource = dataSourceLocator.projectDataSource)
    }
}