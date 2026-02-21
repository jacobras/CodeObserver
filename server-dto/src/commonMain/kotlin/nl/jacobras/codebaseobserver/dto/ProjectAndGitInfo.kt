package nl.jacobras.codebaseobserver.dto

import kotlin.time.Instant

interface ProjectAndGitInfo {
    val projectId: String
    val gitHash: String
    val gitDate: Instant
}