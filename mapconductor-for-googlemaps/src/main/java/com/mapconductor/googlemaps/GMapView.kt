package com.mapconductor.googlemaps

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.node.Ref
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun GMapView(
    state: GMapViewState = rememberGMapViewState(id = "map")
) {
    val isInitialized by state.isInitialized.collectAsState()
    val containerRef = remember { Ref<FrameLayout>() }

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

    LaunchedEffect(isInitialized) {
        if (isInitialized) {
            containerRef.value?.let { container ->
                state.attachTo(container)
            }
        }
    }

}