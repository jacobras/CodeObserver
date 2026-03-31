package nl.jacobras.codeobserver.dto

import kotlin.time.Instant

interface ProjectAndGitInfo {
    val projectId: ProjectId
    val gitHash: GitHash
    val gitDate: Instant
}