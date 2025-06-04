package com.mapconductor.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.map.MapOverlay
import com.mapconductor.core.map.MapOverlayRegistry

// data class OverlayProvider<T>(
//    val compositionLocal: ProvidableCompositionLocal<MutableStateFlow<List<T>>>,
//    val stateFlow: MutableStateFlow<List<T>>,
// )

// @Composable
// fun ProvideOverlayLocals(
//    providers: List<OverlayProvider<*>>,
//    content: @Composable () -> Unit,
// ) {
//    val wrapped =
//        providers.foldRight(content) { provider, acc ->
//            @Suppress("UNCHECKED_CAST")
//            {
//                val local = provider.compositionLocal as ProvidableCompositionLocal<MutableStateFlow<List<Any?>>>
//                val flow = provider.stateFlow as MutableStateFlow<List<Any?>>
//                CompositionLocalProvider(local provides flow) {
//                    acc()
//                }
//            }
//        }
//
//    wrapped()
// }

@Composable
fun <T> CollectAndRenderOverlays(
    map: T?,
    registry: MapOverlayRegistry,
    controller: MapViewController,
) {
    registry.getAll().forEach { overlay ->
        @Suppress("UNCHECKED_CAST")
        val typedOverlay = overlay as MapOverlay<Any>

        val flowState = typedOverlay.flow.collectAsState()

        LaunchedEffect(map, flowState.value) {
            if (map == null) return@LaunchedEffect
            typedOverlay.render(flowState.value, controller)
        }
    }
}
@Composable
fun <T> collectAndRenderOverlays(
    map: T?,
    registry: MapOverlayRegistry,
    controller: MapViewController,
) {
    registry.getAll().forEach { overlay ->
        @Suppress("UNCHECKED_CAST")
        val typedOverlay = overlay as MapOverlay<Any>

        val flowState = typedOverlay.flow.collectAsState()

        LaunchedEffect(map, flowState.value) {
            if (map == null) return@LaunchedEffect
            typedOverlay.render(flowState.value, controller)
        }
    }
}
