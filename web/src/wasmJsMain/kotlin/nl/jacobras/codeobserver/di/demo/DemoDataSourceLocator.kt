package nl.jacobras.codeobserver.di.demo

import nl.jacobras.codeobserver.dashboard.artifacts.ArtifactSizesDataSource
import nl.jacobras.codeobserver.dashboard.buildtimes.BuildTimesDataSource
import nl.jacobras.codeobserver.dashboard.detekt.DetektReportDataSource
import nl.jacobras.codeobserver.dashboard.migrations.MigrationProgressDataSource
import nl.jacobras.codeobserver.dashboard.migrations.MigrationsDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleGraphSettingsDataSource
import nl.jacobras.codeobserver.dashboard.modulegraph.ModuleTypeIdentifiersDataSource
import nl.jacobras.codeobserver.dashboard.trends.TrendsDataSource
import nl.jacobras.codeobserver.di.DataSourceLocator
import nl.jacobras.codeobserver.projects.ProjectDataSource

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