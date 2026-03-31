package nl.jacobras.codeobserver.util.ui

import nl.jacobras.codeobserver.util.data.RequestState

internal data class UiState<Id>(
    val loading: RequestState = RequestState.Idle,
    val saving: RequestState = RequestState.Idle,
    val deleting: Map<Id, RequestState> = emptyMap()
)