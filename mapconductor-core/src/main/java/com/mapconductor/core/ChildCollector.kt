package com.mapconductor.core

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

interface ComponentState {
    val id: String
}

interface ChildCollector<T : ComponentState> {
    val flow: MutableStateFlow<MutableMap<String, T>>

    suspend fun add(state: T)

    fun remove(id: String)

    fun setUpdateHandler(handler: (suspend (T) -> Unit)?)
}

class ChildCollectorImpl<T : ComponentState, FingerPrint>(
    private val asFlow: (T) -> Flow<FingerPrint>,
    private val updateDebounce: Duration,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate),
) : ChildCollector<T> {
    private val scope = scope
    private val addSharedFlow = MutableSharedFlow<T>(1000)
    private val removeSharedFlow = MutableSharedFlow<String>(1000)

    @Volatile private var updateHandler: (suspend (T) -> Unit)? = null
    private val updateJobs = mutableMapOf<String, Job>()

    override val flow = MutableStateFlow<MutableMap<String, T>>(mutableMapOf())

    init {
        scope.launch {
            addSharedFlow.debounceBatch(5.milliseconds, 100).collect { states ->
                val newMap = flow.value.toMutableMap()
                states.forEach { state ->
                    newMap[state.id] = state
                    updateJobs.remove(state.id)?.cancel()
                    startUpdateJob(state)
                }
                flow.value = newMap
            }
        }

        scope.launch {
            removeSharedFlow.debounceBatch(5.milliseconds, 300).collect { ids ->
                val newMap = flow.value.toMutableMap()
                ids.forEach { id ->
                    newMap.remove(id)
                    updateJobs.remove(id)?.cancel()
                }
                flow.value = newMap
            }
        }
    }

    override suspend fun add(state: T) {
        addSharedFlow.emit(state)
    }

    override fun remove(id: String) {
        removeSharedFlow.tryEmit(id)
    }

    override fun setUpdateHandler(handler: (suspend (T) -> Unit)?) {
        updateHandler = handler
    }

    private fun startUpdateJob(state: T) {
        updateJobs[state.id] =
            scope.launch {
                asFlow(state)
                    .debounce(updateDebounce)
                    .collectLatest {
                        updateHandler?.invoke(state)
                    }
            }
    }
}
