package nl.jacobras.codebaseobserver.cli

internal fun runCommand(workingDir: java.io.File?, vararg args: String): String? {
    return try {
        val builder = ProcessBuilder(*args)
        if (workingDir != null) {
            builder.directory(workingDir)
        }
        val process = builder
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exit = process.waitFor()
        if (exit == 0) output else null
    } catch (_: Exception) {
        null
    }
}