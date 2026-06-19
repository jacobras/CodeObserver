package nl.jacobras.codeobserver.server

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

/**
 * Rebuilds the `moduleGraph` table so its primary key is `(projectId, gitHash)` instead of
 * `projectId` alone, allowing one graph snapshot to be retained per commit (history) rather than a
 * single upserted row per project.
 *
 * Runs after `SchemaUtils.create(...)`. SQLite cannot `ALTER` a primary key, so the table is rebuilt:
 * the existing row(s) are copied into a replacement table with the new PK, then it replaces the
 * original. No-op on a fresh database (the table is already created with the composite PK) and on
 * every restart after it has run once.
 */
internal fun JdbcTransaction.migrateModuleGraphToPerCommit() {
    val tableSql = exec(
        "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'moduleGraph'"
    ) { rs ->
        if (rs.next()) rs.getString(1) else null
    }

    // Fresh database: the table was just created with the composite PK already.
    if (tableSql == null) return
    // Already migrated if the primary key clause includes gitHash.
    if (PRIMARY_KEY_WITH_GIT_HASH.containsMatchIn(tableSql)) return

    // Mirror the DDL Exposed produces for ModuleGraphTable, but with the composite primary key. Build
    // the replacement first and drop the original last, so a failure mid-migration never loses data.
    exec("DROP TABLE IF EXISTS moduleGraphNew")
    exec(
        """
        CREATE TABLE moduleGraphNew (
            createdAt BIGINT NOT NULL,
            projectId TEXT NOT NULL,
            gitHash TEXT NOT NULL,
            gitDate BIGINT NOT NULL,
            graph TEXT NOT NULL,
            moduleDetails TEXT DEFAULT '' NOT NULL,
            longestPath TEXT DEFAULT '' NOT NULL,
            CONSTRAINT pk_moduleGraph PRIMARY KEY (projectId, gitHash)
        )
        """.trimIndent()
    )
    exec(
        """
        INSERT INTO moduleGraphNew (createdAt, projectId, gitHash, gitDate, graph, moduleDetails, longestPath)
        SELECT createdAt, projectId, gitHash, gitDate, graph, moduleDetails, longestPath
        FROM moduleGraph
        """.trimIndent()
    )
    exec("DROP TABLE moduleGraph")
    exec("ALTER TABLE moduleGraphNew RENAME TO moduleGraph")
}

private val PRIMARY_KEY_WITH_GIT_HASH = Regex("""PRIMARY KEY\s*\([^)]*gitHash[^)]*\)""")
