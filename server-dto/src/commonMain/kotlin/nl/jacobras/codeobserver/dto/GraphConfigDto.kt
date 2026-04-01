package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
sealed interface GraphConfigDto {
    data class DeprecatedModule(val module: String) : GraphConfigDto
    data class ForbiddenDependency(val a: String, val b: String) : GraphConfigDto
}