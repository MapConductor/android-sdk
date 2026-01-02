package com.mapconductor.core.heatmap

import com.mapconductor.core.debounceBatch
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HeatmapPointCollector(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val addSharedFlow = MutableSharedFlow<HeatmapPointState>(1000)
    private val removeSharedFlow = MutableSharedFlow<String>(1000)
    val flow = MutableStateFlow<MutableMap<String, HeatmapPointState>>(mutableMapOf())

    init {
        scope.launch {
            addSharedFlow.debounceBatch(5.milliseconds, 100).collect { states ->
                val newMap = flow.value.toMutableMap()
                states.forEach { state ->
                    newMap[state.id] = state
                }
                flow.value = newMap
            }
        }

        scope.launch {
            removeSharedFlow.debounceBatch(5.milliseconds, 300).collect { ids ->
                val newMap = flow.value.toMutableMap()
                ids.forEach { id ->
                    newMap.remove(id)
                }
                flow.value = newMap
            }
        }
    }

    suspend fun add(state: HeatmapPointState) {
        addSharedFlow.emit(state)
    }

    fun remove(id: String) {
        removeSharedFlow.tryEmit(id)
    }
}
