package com.mapconductor.example.pages.vectortile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.utils.LoadingDialog
import com.mapconductor.vectorstyle.StyleRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Draws a MapLibre vector style on whichever backend is selected.
 *
 * The point of the page is the provider switcher at the top: Google Maps,
 * MapKit-equivalents, HERE, ArcGIS and the rest cannot render a vector style,
 * yet all of them show this one — because what they are handed is an ordinary
 * raster layer whose tiles were rendered on the device.
 */
@Composable
fun VectorTilePage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    var styleJson by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf<String?>(null) }
    var diagnostics by remember { mutableStateOf<List<String>>(emptyList()) }
    var opacity by remember { mutableFloatStateOf(1.0f) }
    // 256 quarters the pixels per tile but needs four times as many tiles for
    // the same screen — and four times the source fetches. Which wins is not
    // predictable, so it is switchable and measured.
    // null は「地図 SDK の好みに任せる」。ArcGIS の 3D だけ 256 を要求してくる。
    var tileSize by remember { mutableStateOf<Int?>(null) }
    var mapViewState by remember { mutableStateOf<com.mapconductor.core.map.MapViewStateInterface<*>?>(null) }
    // Basemap mode: blank whatever the backend would draw underneath, so the
    // only thing on screen is the style rendered here. What the backend keeps
    // is its own business — this is the part of `MapConductorDesign` that can
    // be tried before the design type itself exists.
    var asBasemap by remember { mutableStateOf(false) }

    // 「描かない」を選べる種別と、それが隠す role。
    //
    // 1 つの種別に role が複数あるのは、種別が**地図の上のもの**で role が
    // **スタイルの中のもの**だから。人が「陸」と呼ぶのは、地図が乗っている
    // 背景（background）＋その上の土地利用（land）＋敷地（landuse）で、
    // 真ん中だけ消しても地面の色は変わらない。「水域」も面（water）と
    // 川筋（waterway）の両方。色のつまみも同じまとめ方をしている。
    // react-sdk の VectorTileLayerPage と同じ並び。
    val categories = remember { VectorTileCategory.all }
    var hidden by remember { mutableStateOf(emptySet<String>()) }
    var palette by remember { mutableStateOf(VectorTileCategory.defaultPalette) }
    // 置き換えではなく混ぜる。本物のベースマップは森・草地・農地を十数通りの
    // 緑で描き分けていて、1 色に潰すとその差が消えて地図に見えなくなる。
    var strength by remember { mutableFloatStateOf(0.6f) }
    var picking by remember { mutableStateOf<String?>(null) }

    /**
     * つまみの状態をそのままルールにしたもの。
     *
     * 外した種別はレイヤーごと落ちるので**描かれない**（背景と同じ色を塗るので
     * はない）。色は `mix` で元の色から寄せる。強さ 0 なら何も書かず、文書は
     * 作者が書いたままになる。
     */
    val rules =
        remember(hidden, palette, strength) {
            // `visible` という名前は使えない: パッチ側の `visible` を隠す。
            val shown = categories.filter { it.key !in hidden }
            val gone = categories.filter { it.key in hidden }
            if (gone.isEmpty() && strength <= 0f) {
                StyleRules.NONE
            } else {
                StyleRules.build {
                    gone.flatMap { it.roles }.forEach { role -> role(role) { visible = false } }
                    if (strength > 0f) {
                        shown.filter { it.tinted }.forEach { category ->
                            val target = palette.getValue(category.key)
                            category.roles.forEach { role -> role(role) { mix(target, strength) } }
                        }
                    }
                }
            }
        }

    // Central Tokyo: the OSMF Shortbread service has street-level detail here,
    // so the layer is obviously doing something at the default zoom.
    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = GeoPoint.fromLatLong(latitude = 35.68049, longitude = 139.76669),
                zoom = 12.0,
                bearing = 0.0,
                tilt = 0.0,
                paddings = null,
            )
        }

    LaunchedEffect(Unit) {
        runCatching { withContext(Dispatchers.IO) { VectorTileStyleLoader.load() } }
            .onSuccess { styleJson = it }
            .onFailure { failure = it.message ?: it.toString() }
    }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { mapViewState = it },
    ) { paddings ->
        Box(
            modifier = modifier.fillMaxSize().padding(paddings),
            contentAlignment = Alignment.Center,
        ) {
            VectorTileMapComponent(
                mapViewState = mapViewState,
                asBasemap = asBasemap,
                styleJson = styleJson,
                opacity = opacity,
                rules = rules,
                tileSize = tileSize,
                onDiagnostics = { diagnostics = it },
            )

            Card(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        // Clear of the attribution band the SDK draws along the
                        // bottom edge. A credit half-hidden behind this demo's
                        // own controls is not a credit -- and this page draws
                        // OpenStreetMap, whose licence requires one.
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 28.dp),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = asBasemap,
                            onClick = { asBasemap = !asBasemap },
                            label = { Text("As basemap", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                        FilterChip(
                            selected = tileSize == null,
                            onClick = { tileSize = null },
                            label = { Text("Auto", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                        listOf(256, 512).forEach { size ->
                            FilterChip(
                                selected = tileSize == size,
                                onClick = { tileSize = size },
                                label = { Text("${size}px", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                    }
                    // 上の段で描くかどうか、下の段でその色。色を持つのは面と線の
                    // 4 つで、ラベルは消すだけ（下の段に四角は出ない）。
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        categories.forEach { category ->
                            FilterChip(
                                selected = category.key !in hidden,
                                onClick = {
                                    hidden =
                                        if (category.key in hidden) {
                                            hidden - category.key
                                        } else {
                                            hidden + category.key
                                        }
                                },
                                label = { Text(category.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        categories.filter { it.tinted }.forEach { category ->
                            Box(
                                modifier =
                                    Modifier
                                        .width(72.dp)
                                        .size(height = 24.dp, width = 72.dp)
                                        .background(
                                            palette.getValue(category.key),
                                            RoundedCornerShape(6.dp),
                                        ).border(1.dp, Color(0x33000000), RoundedCornerShape(6.dp))
                                        .clickable { picking = category.key },
                            )
                        }
                    }
                    Text(
                        text = "Tint strength ${"%.2f".format(strength)}",
                        fontSize = 12.sp,
                    )
                    Slider(
                        value = strength,
                        onValueChange = { strength = it },
                        valueRange = 0f..1f,
                    )
                    Text(
                        text = "Opacity ${"%.2f".format(opacity)}",
                        fontSize = 12.sp,
                    )
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        valueRange = 0f..1f,
                    )
                    // Undrawn layer types and unusable sources are worth showing: the
                    // failure mode that matters is a blank tile with no explanation.
                    diagnostics.forEach { Text(it, fontSize = 11.sp, color = Color(0xFF7A4A00)) }
                    failure?.let { Text("style failed: $it", fontSize = 11.sp, color = Color.Red) }
                }
            }

            // Compose には色のピッカーが無いので、サンプルに要るぶんだけの
            // 見本帳を出す。選べる色の少なさはデモの都合で、API の都合ではない。
            picking?.let { key ->
                AlertDialog(
                    onDismissRequest = { picking = null },
                    confirmButton = { TextButton(onClick = { picking = null }) { Text("Close") } },
                    title = { Text(categories.first { it.key == key }.label, fontSize = 14.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            VectorTileCategory.swatches.chunked(5).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { swatch ->
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(40.dp)
                                                    .background(swatch, RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0x33000000), RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        palette = palette + (key to swatch)
                                                        picking = null
                                                    },
                                        )
                                    }
                                }
                            }
                        }
                    },
                )
            }

            if (styleJson == null && failure == null) {
                LoadingDialog(title = "Vector tiles", message = "Loading vector style…")
            }
        }
    }
}
