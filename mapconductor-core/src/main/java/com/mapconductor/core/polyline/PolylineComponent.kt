package com.mapconductor.core.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.features.IGeoPoint
import android.os.Parcelable

@Composable
fun MapViewScope.Polyline(state: PolylineState) {
    LaunchedEffect(state.fingerPrint()) {
        val newMap = polylineFlow.value.toMutableMap()
        newMap.set(state.id, state)
        polylineFlow.value = newMap
    }
}

@Composable
fun MapViewScope.Polyline(
    points: List<IGeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    extra: Parcelable? = null,
) {
    val state =
        PolylineState(
            points = points,
            id = id,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            geodesic = geodesic,
            extra = extra,
        )
    Polyline(state)
}
