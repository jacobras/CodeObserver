package nl.jacobras.codeobserver.server

import nl.jacobras.codeobserver.server.entity.MetricsTable
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

/**
 * Moves `moduleCount` / `moduleTreeHeight` data out of the `metrics` table and into the
 * `gradleMetrics` table, then removes those columns from `metrics`.
 *
 * Runs after `SchemaUtils.create(...)`. The MigrationUtils diff is empty once `metrics` matches its
 * definition, so this is a no-op on a fresh database and on every restart after it has run once.
 */
internal fun JdbcTransaction.migrateModuleMetricsToGradleMetrics() {
    val migrationNeeded = MigrationUtils.statementsRequiredForDatabaseMigration(
        MetricsTable,
        withLogs = false
    ).isNotEmpty()
    if (!migrationNeeded) return

    // Preserve all module history before metrics loses the columns. INSERT OR IGNORE is safe to
    // repeat if a previous attempt failed partway.
    exec(
        """
        INSERT OR IGNORE INTO gradleMetrics
            (createdAt, projectId, gitHash, gitDate, moduleCount, moduleTreeHeight)
        SELECT createdAt, projectId, gitHash, gitDate, moduleCount, moduleTreeHeight
        FROM metrics
        """.trimIndent()
    )

    // SQLite cannot DROP COLUMN a column referenced by a CHECK constraint, and Exposed generates a
    // signed-integer CHECK for every integer column (chk_metrics_signed_integer_moduleCount/...), so
    // the table has to be rebuilt. Build the replacement first and drop the original last, so a
    // failure mid-migration never loses data. The CREATE mirrors the DDL Exposed produces for
    // MetricsTable, so the result is identical to a freshly created schema.
    exec("DROP TABLE IF EXISTS metricsNew")
    exec(
        """
        CREATE TABLE metricsNew (
            createdAt BIGINT NOT NULL,
            projectId TEXT NOT NULL,
            gitHash TEXT NOT NULL,
            gitDate BIGINT NOT NULL,
            linesOfCode INT DEFAULT 0 NOT NULL,
            CONSTRAINT pk_metrics PRIMARY KEY (projectId, gitHash),
            CONSTRAINT chk_metrics_signed_integer_linesOfCode CHECK (linesOfCode BETWEEN -2147483648 AND 2147483647)
        )
        """.trimIndent()
    )
    exec(
        """
        INSERT INTO metricsNew (createdAt, projectId, gitHash, gitDate, linesOfCode)
        SELECT createdAt, projectId, gitHash, gitDate, linesOfCode
        FROM metrics
        """.trimIndent()
    )
    exec("DROP TABLE metrics")
    exec("ALTER TABLE metricsNew RENAME TO metrics")
}