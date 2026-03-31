package nl.jacobras.codeobserver.util.data

internal sealed class RequestState {
    data object Idle : RequestState()
    data object Working : RequestState()
    data class Error(val type: NetworkError) : RequestState()
}