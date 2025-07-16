package com.mapconductor.core.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.IGeoPoint
import android.os.Parcelable


@Composable
fun MapViewScope.Polyline(state: PolylineState) {
    val rememberState = remember { state }
    SideEffect {
        polylineFlow.value = polylineFlow.value + rememberState
    }
}

@Composable
fun MapViewScope.Polyline(
    points: List<IGeoPoint>,
    id: String? = null,
    strokeColor: Int = android.graphics.Color.BLACK,
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
