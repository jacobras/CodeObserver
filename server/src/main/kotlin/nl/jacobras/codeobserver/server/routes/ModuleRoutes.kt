package nl.jacobras.codeobserver.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.json.Json
import nl.jacobras.codeobserver.dto.GraphConfigDto
import nl.jacobras.codeobserver.dto.GraphModuleDto
import nl.jacobras.codeobserver.dto.GraphModulesDto
import nl.jacobras.codeobserver.dto.GraphVisualInfoDto
import nl.jacobras.codeobserver.dto.ModuleSortOrder
import nl.jacobras.codeobserver.server.entity.ModuleGraphSettingsTable
import nl.jacobras.codeobserver.server.entity.ModuleGraphTable
import nl.jacobras.codeobserver.server.entity.ModuleTypeIdentifiersTable
import nl.jacobras.codeobserver.server.graph.GraphUtil
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal fun Route.moduleRoutes() {
    get("/graphVisualInfo") {
        val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
        if (projectId.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
            return@get
        }
        val groupingThreshold = call.request.queryParameters["groupingThreshold"]?.trim()?.toIntOrNull()
        if (groupingThreshold == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing groupingThreshold"))
            return@get
        }

        val graphRecord = transaction {
            ModuleGraphTable
                .selectAll()
                .where { ModuleGraphTable.projectId eq projectId }
                .orderBy(ModuleGraphTable.gitDate to SortOrder.DESC)
                .limit(1)
                .singleOrNull()
        }
        if (graphRecord == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Record not found"))
            return@get
        }

        val graphMap = Json.decodeFromString<Map<String, List<String>>>(graphRecord[ModuleGraphTable.graph])
        val rawModuleDetails = graphRecord[ModuleGraphTable.moduleDetails]
        val (graphConfig, moduleColors) = transaction {
            val config = ModuleGraphSettingsTable.selectAll()
                .where { ModuleGraphSettingsTable.projectId eq projectId }
                .map {
                    when (val type = it[ModuleGraphSettingsTable.type]) {
                        "deprecatedModule" -> GraphConfigDto.DeprecatedModule(it[ModuleGraphSettingsTable.data])
                        "forbiddenDependency" -> {
                            val parts = it[ModuleGraphSettingsTable.data].split(" -> ")
                            if (parts.size == 2) {
                                GraphConfigDto.ForbiddenDependency(parts[0], parts[1])
                            } else {
                                error("Invalid data format: ${it[ModuleGraphSettingsTable.data]}")
                            }
                        }
                        else -> error("Unsupported type: $type")
                    }
                }
            val typeColors = ModuleTypeIdentifiersTable.selectAll()
                .where { ModuleTypeIdentifiersTable.projectId eq projectId }
                .associate { it[ModuleTypeIdentifiersTable.typeName] to it[ModuleTypeIdentifiersTable.color] }
            val moduleTypeMap = parseModuleDetails(rawModuleDetails)
            val colors = moduleTypeMap
                .mapValues { (_, typeName) -> typeColors[typeName].orEmpty() }
                .filter { it.value.isNotEmpty() }
            config to colors
        }
        val info = GraphVisualInfoDto(
            modules = graphMap,
            config = graphConfig,
            moduleColors = moduleColors
        )
        call.respond(info)
    }
    get("/modules") {
        val projectId = call.request.queryParameters["projectId"]?.trim().orEmpty()
        if (projectId.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing projectId"))
            return@get
        }
        val sortOrderId = call.request.queryParameters["sort"]?.trim().orEmpty()
        val sortOrder = ModuleSortOrder.fromId(sortOrderId) ?: run {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Unsupported sort order: $sortOrderId")
            )
            return@get
        }

        val graphRecord = transaction {
            ModuleGraphTable
                .selectAll()
                .where { ModuleGraphTable.projectId eq projectId }
                .orderBy(ModuleGraphTable.gitDate to SortOrder.DESC)
                .limit(1)
                .singleOrNull()
        }
        if (graphRecord == null) {
            call.respond(GraphModulesDto())
            return@get
        }

        val graphMap = Json.decodeFromString<Map<String, List<String>>>(graphRecord[ModuleGraphTable.graph])

        val modules = when (sortOrder) {
            ModuleSortOrder.BetweennessCentrality -> {
                val betweennessCentrality = GraphUtil.calculateBetweennessCentralityScore(graphMap)
                betweennessCentrality.map { GraphModuleDto(it.key, it.value.toInt()) }
            }
            ModuleSortOrder.Alphabetical -> {
                val modules = (graphMap.keys + graphMap.values.flatten()).distinct().sorted()
                modules.map { GraphModuleDto(it, 0) }
            }
        }
        call.respond(
            GraphModulesDto(
                modules = modules,
                longestPath = graphRecord[ModuleGraphTable.longestPath].let {
                    if (it.isBlank()) {
                        emptyList()
                    } else {
                        it.split(",")
                    }
                }
            )
        )
    }
}

/**
 * Parses moduleDetails text (e.g. "moduleA[android],moduleB[kmp]") into a map of module name to type name.
 */
private fun parseModuleDetails(moduleDetails: String): Map<String, String> {
    if (moduleDetails.isBlank()) return emptyMap()
    return moduleDetails.split(",").mapNotNull { entry ->
        val bracketIndex = entry.indexOf('[')
        if (bracketIndex < 0 || !entry.endsWith(']')) return@mapNotNull null
        val module = entry.substring(0, bracketIndex)
        val type = entry.substring(bracketIndex + 1, entry.length - 1)
        if (module.isBlank() || type.isBlank()) null else module to type
    }.toMap()
}