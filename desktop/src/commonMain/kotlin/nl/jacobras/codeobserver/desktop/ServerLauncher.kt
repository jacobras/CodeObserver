package nl.jacobras.codeobserver.desktop

import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import me.sujanpoudel.utils.paths.appDataDirectory
import nl.jacobras.codeobserver.server.module
import java.io.File

internal class ServerLauncher {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private val dbFile = File(appDataDirectory(appId = "nl.jacobras.codeobserver").toString(), "data.db")

    val started = MutableStateFlow(false)

    fun start() {
        if (started.value) {
            return
        }

        val resourcesDir = System.getProperty("compose.application.resources.dir")
        val webRoot = resourcesDir?.let { File(it, "web") } ?: error("Cannot find webRoot dir")

        server = embeddedServer(
            factory = Netty,
            port = 8080,
            module = {
                module(
                    webRoot = webRoot,
                    defaultDbPath = dbFile.absolutePath
                )
            }
        ).also { it.start(wait = false) }
        started.value = true
    }

    fun stop() {
        if (!started.value) {
            return
        }
        server?.stop()
        server = null
        started.value = false
    }
}