package nl.jacobras.codebaseobserver.cli

internal fun runCommand(vararg args: String): String? {
    return try {
        val process = ProcessBuilder(*args)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exit = process.waitFor()
        if (exit == 0) output else null
    } catch (_: Exception) {
        null
    }
}