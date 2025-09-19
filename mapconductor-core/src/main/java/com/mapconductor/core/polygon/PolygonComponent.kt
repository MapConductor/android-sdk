package com.mapconductor.core.polygon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.GeoPoint
import android.os.Parcelable

@Composable
fun MapViewScope.Polygon(state: PolygonState) {
    LaunchedEffect(state.fingerPrint()) {
        val newMap = polygonFlow.value.toMutableMap()
        newMap.set(state.id, state)
        polygonFlow.value = newMap
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
    extra: Parcelable? = null,
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
        )
    Polygon(state)
}
