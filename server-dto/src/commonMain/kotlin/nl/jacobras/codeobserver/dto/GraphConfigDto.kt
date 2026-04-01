package nl.jacobras.codeobserver.dto

import kotlinx.serialization.Serializable

@Serializable
sealed interface GraphConfigDto {

    @Serializable
    data class DeprecatedModule(val module: String) : GraphConfigDto

    @Serializable
    data class ForbiddenDependency(val a: String, val b: String) : GraphConfigDto
}