package nl.jacobras.codebaseobserver.ui

import nl.jacobras.codebaseobserver.data.RequestState

internal data class UiState<Id>(
    val loading: RequestState = RequestState.Idle,
    val saving: RequestState = RequestState.Idle,
    val deleting: Map<Id, RequestState> = emptyMap()
)