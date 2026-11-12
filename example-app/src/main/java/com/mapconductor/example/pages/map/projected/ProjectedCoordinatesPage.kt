package com.mapconductor.example.pages.map.projected

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.marker.Marker
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewHolderInterface
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.MapCoordinateSystem
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold

/**
 * 投影座標（地図エンジンの座標系での x / y）を見るサンプル。**画面座標ではない。**
 *
 * `MapViewHolder.toProjectedPoint` / `fromProjectedPoint` と `coordinateSystem` を
 * 使う。地図の中心と、タップした地点について、
 *
 * - 地図エンジンの座標系での x / y
 * - その x / y を緯度経度へ戻した値と、元の地点とのずれ（メートル）
 *
 * を出す。ずれが 0 に近ければ往復できている。
 *
 * react-sdk / ios-sdk の同ページと同じカメラ・同じ項目にしてある。
 */
@Composable
fun ProjectedCoordinatesPage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val initCameraPosition =
        MapCameraPosition(
            position = GeoPoint(latitude = 35.681236, longitude = 139.767125),
            zoom = 12.0,
        )
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }

    DemoMapPageScaffold(
        initSelect = 0,
        menuItems = DefaultMapViewItems(initCameraPosition),
        modifier = modifier,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { mapViewState = it },
    ) {
        mapViewState?.let { ProjectedCoordinatesMapComponent(mapViewState = it) }
    }
}

@Composable
private fun ProjectedCoordinatesMapComponent(mapViewState: MapViewStateInterface<*>) {
    var center by remember(mapViewState) { mutableStateOf<GeoPointInterface?>(null) }
    var tapped by remember(mapViewState) { mutableStateOf<GeoPointInterface?>(null) }
    // ホルダは地図が出来てから付く。読み出しを合成に任せると、付いた後に組み直しが
    // 起きず（onMapLoaded の中心が初期値と同じだと state が変わらない）表示が
    // 取り残されるので、地図の通知で取り直して state に持つ。
    var holder by remember(mapViewState) { mutableStateOf(mapViewState.getMapViewHolder()) }
    var markers by remember(mapViewState) { mutableStateOf(emptyList<MarkerState>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            state = mapViewState,
            onMapLoaded = {
                holder = mapViewState.getMapViewHolder()
                center = mapViewState.cameraPosition.position
            },
            onCameraMove = { position ->
                holder = mapViewState.getMapViewHolder()
                center = position.position
            },
            onMapClick = { point ->
                holder = mapViewState.getMapViewHolder()
                tapped = point
                markers = dropMarkers(point, holder)
            },
        ) {
            markers.forEach { Marker(it) }
        }

        Card(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .widthIn(max = 380.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Projected Coordinates",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                InfoLine("Tap the map to project the tapped point.")
                InfoLine("Markers: red T = tapped, blue P = back from the projected point.")
                InfoLine("Coordinate system: ${holder?.coordinateSystem?.let(::formatSystem) ?: UNAVAILABLE}")
                ProjectionLines("Center", center, holder)
                ProjectionLines("Tapped", tapped, holder)
            }
        }
    }
}

/**
 * タップした地点（赤）と、その投影座標を緯度経度へ戻した地点（青・小）に落とすマーカー。
 *
 * 投影と逆投影が合っていれば、青は赤のピン先に重なる。青は `iconSize` で小さくし、
 * `zIndex = 1` で赤より手前に描く（重なり順は追加した順ではなく zIndex で決まる）。
 * タップのたびに新しいマーカーにするので、Drop がそのつど再生される。
 */
private fun dropMarkers(
    tapped: GeoPointInterface,
    holder: MapViewHolderInterface<*, *>?,
): List<MarkerState> {
    val back = holder?.toProjectedPoint(tapped)?.let { holder.fromProjectedPoint(it) }
    return listOfNotNull(
        MarkerState(
            position = tapped,
            icon = DefaultMarkerIcon(fillColor = Color(0xFFE53935), label = "T"),
            animation = MarkerAnimation.Drop,
        ),
        back?.let {
            MarkerState(
                position = it,
                icon = DefaultMarkerIcon(fillColor = Color(0xFF1E88E5), label = "P", iconSize = 28.dp),
                animation = MarkerAnimation.Drop,
                zIndex = 1,
            )
        },
    )
}

/** 1 地点ぶん: 緯度経度、投影座標、戻した緯度経度と元とのずれ。 */
@Composable
private fun ProjectionLines(
    label: String,
    point: GeoPointInterface?,
    holder: MapViewHolderInterface<*, *>?,
) {
    val projected = point?.let { holder?.toProjectedPoint(it) }
    val back = projected?.let { holder?.fromProjectedPoint(it) }
    InfoLine("$label: ${formatPoint(point)}")
    InfoLine("  Projected: ${projected?.let { "x=%.2f, y=%.2f".format(it.x, it.y) } ?: UNAVAILABLE}")
    InfoLine(
        "  Back: ${formatPoint(back)}" +
            if (point != null && back != null) {
                " (error %.3f m)".format(Spherical.computeDistanceBetween(point, back))
            } else {
                ""
            },
    )
}

@Composable
private fun InfoLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 1.dp),
    )
}

/** 取得できないときの表示。react / ios の同ページと揃える。 */
private const val UNAVAILABLE = "Unavailable"

private fun formatSystem(system: MapCoordinateSystem): String = "${system.code ?: "(engine-defined)"} (${system.unit})"

private fun formatPoint(point: GeoPointInterface?): String =
    point?.let { GeoPoint.from(it).toUrlValue(6) } ?: UNAVAILABLE
