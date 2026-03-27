package nl.jacobras.codebaseobserver.cli.util

import nl.jacobras.codebaseobserver.dto.GitHash
import java.nio.file.Path
import kotlin.time.Instant

internal object GitInfoCollector {

    fun getGitHash(workingDirPath: Path): GitHash {
        val workingDir = workingDirPath.toFile()
        val res = runCommand(workingDir, "git", "rev-parse", "HEAD")?.trim().orEmpty()
        require(res.isNotEmpty()) {
            "Could not determine git hash. Make sure you are in a git repository."
        }
        return GitHash(res)
    }

    fun getGitDate(workingDirPath: Path): Instant {
        val workingDir = workingDirPath.toFile()
        val res = runCommand(workingDir, "git", "show", "-s", "--format=%cI", "HEAD")?.trim().orEmpty()
        require(res.isNotEmpty()) {
            "Could not determine git date. Make sure you are in a git repository."
        }
        return Instant.parse(res)
    }
}