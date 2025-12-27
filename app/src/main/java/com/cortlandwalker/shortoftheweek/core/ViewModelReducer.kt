package com.cortlandwalker.shortoftheweek.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class ViewModelReducer<State, Action, Effect>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected val currentState: State
        get() = _state.value

    // Scope is now natively provided by ViewModel
    protected val scope = viewModelScope

    // Abstract methods your Reducers already implement
    abstract suspend fun process(action: Action)
    open fun onLoadAction(): Action? = null

    // Helper to dispatch actions
    fun postAction(action: Action) {
        scope.launch {
            process(action)
        }
    }

    // Helper to update state safely
    protected suspend fun state(reducer: (State) -> State) {
        _state.emit(reducer(_state.value))
    }

    // Helper to emit effects
    protected suspend fun emit(effect: Effect) {
        _effect.send(effect)
    }
}
