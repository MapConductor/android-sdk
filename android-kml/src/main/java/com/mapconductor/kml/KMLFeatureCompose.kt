package com.mapconductor.kml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import com.mapconductor.compose.MapViewScope
import com.mapconductor.core.OverlayCollectorInterface

val LocalKMLFeatureCollector =
    compositionLocalOf<OverlayCollectorInterface<KMLFeatureState>> {
        error("KMLFeature must be placed inside a KMLLayer composable")
    }

@Composable
fun MapViewScope.KMLFeature(state: KMLFeatureState) {
    val collector = LocalKMLFeatureCollector.current
    LaunchedEffect(state) {
        collector.add(state)
    }
    DisposableEffect(state.id) {
        onDispose {
            collector.remove(state.id)
        }
    }
}

@Composable
fun MapViewScope.KMLFeatures(states: List<KMLFeatureState>) {
    val collector = LocalKMLFeatureCollector.current
    LaunchedEffect(states, states.size) {
        collector.replaceAll(states)
    }
    DisposableEffect(Unit) {
        onDispose {
            collector.replaceAll(emptyList())
        }
    }
}
