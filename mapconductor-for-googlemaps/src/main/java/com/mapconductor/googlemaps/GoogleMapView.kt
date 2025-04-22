package com.mapconductor.googlemaps

import android.annotation.SuppressLint
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.viewinterop.AndroidView
import com.mapconductor.core.LocalMarkerCollector
import com.mapconductor.core.MarkerData
import com.mapconductor.core.MarkerDataWithHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(FlowPreview::class)
@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    state: GoogleMapViewState = rememberGMapViewState(id = "map"),
    content: @Composable GoogleMapViewScope.() -> Unit,
) {
    val isInitialized by state.isInitialized.collectAsState()
    val containerRef = remember { Ref<FrameLayout>() }
    val markersFlow = remember { MutableStateFlow<List<MarkerDataWithHandler>>(emptyList()) }
    val scope = remember { GoogleMapViewScope() }

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
        modifier = modifier,
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