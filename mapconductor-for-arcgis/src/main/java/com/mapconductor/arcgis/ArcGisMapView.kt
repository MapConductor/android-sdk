package com.mapconductor.arcgis

import android.app.ActionBar
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ArcGisMapView(
    state: ArcGisMapViewState = rememberArcGisMapViewState(id = "map")
) {
    val isInitialized by state.isInitialized.collectAsState()
    val containerRef = remember { Ref<FrameLayout>() }

    AndroidView(
        factory = { context ->
            FrameLayout(context).also { container ->
                container.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
                containerRef.value = container
//                if (isInitialized) {
//                    state.attachTo(container)
//                }
                Button(context).also {
                    it.setBackgroundColor(Color.WHITE)
                    it.layoutParams = ViewGroup.LayoutParams(300, 300)
                    it.isClickable = true
                    container.addView(it)
                    it.setOnClickListener {
                        (state as ArcGisMapViewState).attachTo(container)
                    }
                }
            }
        },
        update = { container ->
//            if (isInitialized) {
//                state.attachTo(container)
//            }
        },
    )

//    LaunchedEffect(isInitialized) {
//        if (isInitialized) {
//            containerRef.value?.let { container ->
//                state.attachTo(container)
//            }
//        }
//    }

//    val context = LocalContext.current
//    val lifecycle = LocalLifecycleOwner.current.lifecycle
//    DisposableEffect(lifecycle) {
//        val observer = object : DefaultLifecycleObserver {
//            override fun onResume(owner: LifecycleOwner) {
//                state.onResume(owner)
//            }
//            override fun onPause(owner: LifecycleOwner) {
//                state.onPause(owner)
//            }
//            override fun onDestroy(owner: LifecycleOwner) {
//                state.cancelCoroutine()
//                // ここでActivityが本当に終了するか確認
//                val activity = context.findActivity()
//                if (activity != null &&
//                    activity.isFinishing &&
//                    !activity.isChangingConfigurations
//                ) {
//                    MapViewHolderStore.clear("map", owner)  // Execute mapView.destroy internally
//                }
//            }
//        }
//
//        lifecycle.addObserver(observer)
//        onDispose { lifecycle.removeObserver(observer) }
//    }
}