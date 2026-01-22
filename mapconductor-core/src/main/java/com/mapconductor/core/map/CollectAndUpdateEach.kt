package com.mapconductor.core.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.mapconductor.core.ComponentState
import kotlin.time.Duration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
internal fun <T : ComponentState, FingerPrint> CollectAndUpdateEach(
    states: kotlinx.coroutines.flow.StateFlow<MutableMap<String, T>>,
    debounce: Duration,
    asFlow: (T) -> Flow<FingerPrint>,
    onUpdate: suspend (T) -> Unit,
) {
    val snapshot = states.collectAsState()
    snapshot.value.values.forEach { state ->
        LaunchedEffect(state.id) {
            asFlow(state).debounce(debounce).collectLatest {
                onUpdate(state)
            }
        }
    }
}
