package nl.jacobras.codebaseobserver.di.demo

import nl.jacobras.codebaseobserver.dashboard.artifacts.ArtifactSizesDataSource
import nl.jacobras.codebaseobserver.dashboard.buildtimes.BuildTimesDataSource
import nl.jacobras.codebaseobserver.dashboard.detekt.DetektReportDataSource
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationProgressDataSource
import nl.jacobras.codebaseobserver.dashboard.migrations.MigrationsDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleGraphSettingsDataSource
import nl.jacobras.codebaseobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSource
import nl.jacobras.codebaseobserver.dashboard.trends.TrendsDataSource
import nl.jacobras.codebaseobserver.di.DataSourceLocator
import nl.jacobras.codebaseobserver.projects.ProjectDataSource

internal object DemoDataSourceLocator : DataSourceLocator {
    override val artifactSizesDataSource: ArtifactSizesDataSource = DemoArtifactSizesDataSource()
    override val buildTimesDataSource: BuildTimesDataSource = DemoBuildTimesDataSource()
    override val detektReportDataSource: DetektReportDataSource = DemoDetektReportDataSource()
    override val migrationProgressDataSource: MigrationProgressDataSource = DemoMigrationProgressDataSource()
    override val migrationsDataSource: MigrationsDataSource = DemoMigrationsDataSource()
    override val moduleGraphDataSource: ModuleGraphDataSource = DemoModuleGraphDataSource()
    override val moduleGraphSettingsDataSource: ModuleGraphSettingsDataSource = DemoModuleGraphSettingsDataSource()
    override val moduleTypeIdentifiersDataSource: ModuleTypeIdentifiersDataSource =
        DemoModuleTypeIdentifiersDataSource()
    override val projectDataSource: ProjectDataSource = DemoProjectDataSource()
    override val trendsDataSource: TrendsDataSource = DemoTrendsDataSource()
}