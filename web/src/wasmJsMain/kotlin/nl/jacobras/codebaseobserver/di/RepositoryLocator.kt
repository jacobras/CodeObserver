package nl.jacobras.codebaseobserver.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.jacobras.codebaseobserver.projects.ProjectDataSource
import nl.jacobras.codebaseobserver.projects.ProjectRepository

/**
 * Placeholder until I've set up Koin.
 */
internal object RepositoryLocator {
    private val httpClient = HttpClient(Js) {
        defaultRequest {
            url("/")
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(dataSource = ProjectDataSource(httpClient))
    }
}