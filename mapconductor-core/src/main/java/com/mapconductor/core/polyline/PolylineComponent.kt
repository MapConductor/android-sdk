package com.mapconductor.core.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds
import java.io.Serializable

@Composable
fun MapViewScope.Polyline(state: PolylineState) {
    LaunchedEffect(state.fingerPrint()) {
        val newMap = polylineFlow.value.toMutableMap()
        newMap.set(state.id, state)
        polylineFlow.value = newMap
    }

    DisposableEffect(state.id) {
        onDispose {
            polylineRemoveSharedFlow.tryEmit(state.id)
        }
    }
}

@Composable
fun MapViewScope.Polyline(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    extra: Serializable? = null,
    onClick: OnPolylineEventHandler? = null,
) {
    val state =
        PolylineState(
            points = points,
            id = id,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            geodesic = geodesic,
            extra = extra,
            onClick = onClick,
        )
    Polyline(state)
}

@Composable
fun MapViewScope.Polyline(
    bounds: GeoRectBounds,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    extra: Serializable? = null,
    onClick: OnPolylineEventHandler? = null,
) {
    bounds.northEast?.let { ne ->
        bounds.southWest?.let { sw ->
            val points = listOf(
                ne,
                GeoPointImpl.fromLatLong(sw.latitude, ne.longitude),
                sw,
                GeoPointImpl.fromLatLong(ne.latitude, sw.longitude),
                ne,
            )
            Polyline(
                points = points,
                id = id,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                geodesic = geodesic,
                extra = extra,
                onClick = onClick,
            )
        }
    }
}
