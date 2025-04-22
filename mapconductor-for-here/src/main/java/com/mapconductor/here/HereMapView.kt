package com.mapconductor.here

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.viewinterop.AndroidView
import com.mapconductor.core.LocalMarkerCollector
import com.mapconductor.core.MarkerDataWithHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun HereMapView(
    modifier: Modifier = Modifier,
    state: HereMapViewState = rememberHereMapViewState(
        id = "map",
    ),
    content: @Composable HereMapViewScope.() -> Unit,
) {
    val isInitialized by state.isInitialized.collectAsState()
    val containerRef = remember { Ref<FrameLayout>() }
    val markersFlow = remember { MutableStateFlow<List<MarkerDataWithHandler>>(emptyList()) }
    val scope = remember { HereMapViewScope() }

    CompositionLocalProvider(LocalMarkerCollector provides markersFlow) {
        with(scope) {
            content()
        }
    }

    LaunchedEffect(markersFlow.collectAsState().value) {
        markersFlow
            .debounce(300)
            .collectLatest { markers : List<MarkerDataWithHandler> ->
                state.addMarkers(markers)
            }
    }

    AndroidView(
        factory = { context ->
            FrameLayout(context).also { container ->
                containerRef.value = container
                if (isInitialized) {
                    state.attachTo(container)
                }
            }
        },
        update = { container ->
            if (isInitialized) {
                state.attachTo(container)
            }
        },
    )

//    LaunchedEffect(isInitialized) {
//        if (isInitialized) {
//            containerRef.value?.let { container ->
//                state.attachTo(container)
//            }
//        }
//    }
}