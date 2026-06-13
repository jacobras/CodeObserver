package nl.jacobras.codeobserver.server

import co.touchlab.kermit.Logger
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import nl.jacobras.codeobserver.dto.UserRole
import nl.jacobras.codeobserver.server.auth.ApiKeyService
import nl.jacobras.codeobserver.server.auth.PasswordHasher
import nl.jacobras.codeobserver.server.auth.SESSION_TTL_MS
import nl.jacobras.codeobserver.server.auth.SqliteSessionStorage
import nl.jacobras.codeobserver.server.auth.UserPrincipal
import nl.jacobras.codeobserver.server.auth.UserSession
import nl.jacobras.codeobserver.server.auth.apiKey
import nl.jacobras.codeobserver.server.auth.purgeExpiredSessions
import nl.jacobras.codeobserver.server.entity.ArtifactSizesTable
import nl.jacobras.codeobserver.server.entity.BuildTimesTable
import nl.jacobras.codeobserver.server.entity.DetektReportsTable
import nl.jacobras.codeobserver.server.entity.MetricsTable
import nl.jacobras.codeobserver.server.entity.MigrationProgressTable
import nl.jacobras.codeobserver.server.entity.MigrationsTable
import nl.jacobras.codeobserver.server.entity.ModuleGraphSettingsTable
import nl.jacobras.codeobserver.server.entity.ModuleGraphTable
import nl.jacobras.codeobserver.server.entity.ModuleTypeIdentifiersTable
import nl.jacobras.codeobserver.server.entity.ProjectsTable
import nl.jacobras.codeobserver.server.entity.ServerSettingsTable
import nl.jacobras.codeobserver.server.entity.SessionsTable
import nl.jacobras.codeobserver.server.entity.UsersTable
import nl.jacobras.codeobserver.server.routes.artifactSizeRoutes
import nl.jacobras.codeobserver.server.routes.authRoutes
import nl.jacobras.codeobserver.server.routes.buildTimeRoutes
import nl.jacobras.codeobserver.server.routes.detektReportRoutes
import nl.jacobras.codeobserver.server.routes.metricRoutes
import nl.jacobras.codeobserver.server.routes.migrationRoutes
import nl.jacobras.codeobserver.server.routes.moduleGraphSettingsRoutes
import nl.jacobras.codeobserver.server.routes.moduleRoutes
import nl.jacobras.codeobserver.server.routes.moduleTypeIdentifierRoutes
import nl.jacobras.codeobserver.server.routes.projectRoutes
import nl.jacobras.codeobserver.server.routes.userRoutes
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import kotlin.time.Duration.Companion.hours

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module(
    webRoot: File = File("app/web"),
    defaultDbPath: String = "data/app.db"
) {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }

    val dbPath = System.getenv("DB_PATH") ?: defaultDbPath
    val dbFile = File(dbPath)
    dbFile.parentFile?.mkdirs()
    Database.connect("jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(
            ArtifactSizesTable,
            BuildTimesTable,
            DetektReportsTable,
            MetricsTable,
            MigrationProgressTable,
            MigrationsTable,
            ModuleGraphSettingsTable,
            ModuleGraphTable,
            ModuleTypeIdentifiersTable,
            ProjectsTable,
            ServerSettingsTable,
            SessionsTable,
            UsersTable,
        )
        if (UsersTable.selectAll().empty()) {
            UsersTable.insert {
                it[username] = "admin"
                it[passwordHash] = PasswordHasher.hash("admin")
                it[role] = UserRole.ADMIN.name
            }
            Logger.w { "Created default user 'admin' with password 'admin'. Change the password immediately." }
        }
        ApiKeyService.ensureKeyExists()
    }
    launch {
        while (isActive) {
            purgeExpiredSessions()
            delay(1.hours)
        }
    }

    install(Sessions) {
        cookie<UserSession>("co_session", SqliteSessionStorage()) {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.extensions["SameSite"] = "Lax"
            cookie.maxAgeInSeconds = SESSION_TTL_MS / 1000
            cookie.secure = BuildConfig.RELEASE
            serializer = object : SessionSerializer<UserSession> {
                override fun serialize(session: UserSession): String = Json.encodeToString(session)
                override fun deserialize(text: String): UserSession = Json.decodeFromString(text)
            }
        }
    }
    install(Authentication) {
        session<UserSession>("auth-session") {
            validate { session ->
                transaction {
                    UsersTable
                        .selectAll()
                        .where { UsersTable.username eq session.username }
                        .singleOrNull()
                }?.let {
                    UserPrincipal(
                        username = it[UsersTable.username],
                        role = UserRole.valueOf(it[UsersTable.role])
                    )
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not logged in"))
            }
        }
        apiKey("api-key") {
            validate = { ApiKeyService.isValid(it) }
        }
    }

    routing {
        staticFiles("/", webRoot) {
            default("index.html")
        }
        staticFiles("/dev", File("../web/build/dist/wasmJs/developmentExecutable")) {
            default("index.html")
        }
        authRoutes()
        authenticate("auth-session", "api-key") {
            projectRoutes()
            metricRoutes()
            artifactSizeRoutes()
            buildTimeRoutes()
            detektReportRoutes()
            moduleRoutes()
            migrationRoutes()
            moduleGraphSettingsRoutes()
            moduleTypeIdentifierRoutes()
        }
        authenticate("auth-session") {
            userRoutes()
        }
    }
}