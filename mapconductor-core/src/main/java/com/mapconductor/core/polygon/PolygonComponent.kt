package com.mapconductor.core.polygon

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
fun MapViewScope.Polygon(state: PolygonState) {
    LaunchedEffect(state.fingerPrint()) {
        val newMap = polygonFlow.value.toMutableMap()
        newMap.set(state.id, state)
        polygonFlow.value = newMap
    }

    DisposableEffect(state.id) {
        onDispose {
            polygonRemoveSharedFlow.tryEmit(state.id)
        }
    }
}

@Composable
fun MapViewScope.Polygon(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Serializable? = null,
    onClick: OnPolygonEventHandler? = null,
) {
    val state =
        PolygonState(
            points = points,
            id = id,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
            geodesic = geodesic,
            extra = extra,
            onClick = onClick,
        )
    Polygon(state)
}

@Composable
fun MapViewScope.Polygon(
    bounds: GeoRectBounds,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    extra: Serializable? = null,
    onClick: OnPolygonEventHandler? = null,
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
            Polygon(
                points = points,
                id = id,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                fillColor = fillColor,
                geodesic = geodesic,
                extra = extra,
                onClick = onClick,
            )
        }
    }
}
