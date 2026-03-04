package com.mapconductor.core.polygon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import java.io.Serializable

@Composable
fun MapViewScope.Polygon(state: PolygonState) {
    val collector = LocalPolygonCollector.current
    LaunchedEffect(state) {
        if (state.holes.size > 1) {
            state.unionHolesInPlace()
        }
        collector.add(state)
    }

    DisposableEffect(state.id) {
        onDispose {
            collector.remove(state.id)
        }
    }
}

@Composable
fun MapViewScope.Polygon(
    points: List<GeoPointInterface>,
    holes: List<List<GeoPointInterface>> = emptyList(),
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color.Transparent,
    geodesic: Boolean = false,
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolygonEventHandler? = null,
) {
    val state =
        PolygonState(
            points = points,
            holes = holes,
            id = id,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            fillColor = fillColor,
            geodesic = geodesic,
            zIndex = zIndex,
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
    zIndex: Int = 0,
    extra: Serializable? = null,
    onClick: OnPolygonEventHandler? = null,
) {
    bounds.northEast?.let { ne ->
        bounds.southWest?.let { sw ->
            val points =
                listOf(
                    ne,
                    GeoPoint.fromLatLong(sw.latitude, ne.longitude),
                    sw,
                    GeoPoint.fromLatLong(ne.latitude, sw.longitude),
                    ne,
                )
            Polygon(
                points = points,
                id = id,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                fillColor = fillColor,
                geodesic = geodesic,
                zIndex = zIndex,
                extra = extra,
                onClick = onClick,
            )
        }
    }
}
