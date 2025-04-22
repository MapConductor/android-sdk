package com.mapconductor.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow

data class MarkerData(
    val pointBase: GeoPointBase,
    val icon: String?,
)

typealias MarkerClickHandler = (MarkerData) -> Unit
typealias MarkerDataWithHandler = Pair<MarkerData, MarkerClickHandler>

val LocalMarkerCollector = compositionLocalOf<MutableStateFlow<List<MarkerDataWithHandler>>> {
    error("Marker must be under the <MapView />")
}
@Composable
fun MapViewScope.Marker(geoPoint: GeoPointBase, icon: String? = null, onClick: MarkerClickHandler = {}) {
    val collector = LocalMarkerCollector.current
    val marker = remember {
        MarkerDataWithHandler(MarkerData(geoPoint, icon), onClick) }
    SideEffect {
        collector.value = collector.value + marker
    }
}