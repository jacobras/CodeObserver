package nl.jacobras.codebaseobserver.data

internal data class UiState<Id>(
    val loading: RequestState = RequestState.Idle,
    val saving: RequestState = RequestState.Idle,
    val deleting: Map<Id, RequestState> = emptyMap()
)