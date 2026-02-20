package nl.jacobras.codebaseobserver.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.server.dto.CountRequest
import nl.jacobras.codebaseobserver.server.dto.UpdateCountRequest
import nl.jacobras.codebaseobserver.server.entity.CountRecord
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File
import java.time.Instant

object CountsTable : Table("counts") {
    val createdAt = text("createdAt")
    val gitHash = text("gitHash")
    val gitDate = text("gitDate")
    val linesOfCode = integer("linesOfCode")
    override val primaryKey = PrimaryKey(gitHash)
}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }

    val dbPath = System.getenv("DB_PATH") ?: "/data/app.db"
    val dbFile = File(dbPath)
    dbFile.parentFile?.mkdirs()
    Database.connect("jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC")
    transaction { SchemaUtils.create(CountsTable) }

    routing {
        staticFiles("/", File("app/web")) {
            default("index.html")
        }
        staticFiles("/dev", File("../web/build/dist/wasmJs/developmentExecutable")) {
            default("index.html")
        }
        post("/counts") {
            val request = call.receive<CountRequest>()
            val createdAt = Instant.now().toString()
            transaction {
                CountsTable.insert {
                    it[gitHash] = request.gitHash
                    it[gitDate] = request.gitDate
                    it[linesOfCode] = request.linesOfCode
                    it[CountsTable.createdAt] = createdAt
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("status" to "stored"))
        }
        get("/counts") {
            val records = transaction {
                CountsTable.selectAll().orderBy(CountsTable.gitDate to SortOrder.ASC).map {
                    CountRecord(
                        gitHash = it[CountsTable.gitHash],
                        gitDate = it[CountsTable.gitDate],
                        linesOfCode = it[CountsTable.linesOfCode],
                        createdAt = it[CountsTable.createdAt]
                    )
                }
            }
            call.respond(records)
        }
        put("/counts/{gitHash}") {
            val gitHash = call.parameters["gitHash"]
            if (gitHash.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing gitHash"))
                return@put
            }
            val request = call.receive<UpdateCountRequest>()
            val updatedRows = transaction {
                CountsTable.update({ CountsTable.gitHash eq gitHash }) {
                    it[gitDate] = request.gitDate
                    it[linesOfCode] = request.linesOfCode
                }
            }
            if (updatedRows == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Record not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
            }
        }
        delete("/counts/{gitHash}") {
            val gitHash = call.parameters["gitHash"]
            if (gitHash.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing gitHash"))
                return@delete
            }
            val deletedRows = transaction {
                CountsTable.deleteWhere { CountsTable.gitHash eq gitHash }
            }
            if (deletedRows == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Record not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            }
        }
    }
}