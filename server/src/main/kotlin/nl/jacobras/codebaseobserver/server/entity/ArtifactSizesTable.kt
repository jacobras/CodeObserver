package nl.jacobras.codebaseobserver.server.entity

import org.jetbrains.exposed.v1.core.Table

internal object ArtifactSizesTable : Table("artifactSizes") {
    val createdAt = long("createdAt")
    val projectId = text("projectId")
    val name = text("name")
    val semVer = text("semVer")
    val size = long("sizeBytes")
    override val primaryKey = PrimaryKey(projectId, name, semVer)
}