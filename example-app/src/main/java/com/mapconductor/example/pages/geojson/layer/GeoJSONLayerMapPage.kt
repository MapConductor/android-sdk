package com.mapconductor.example.pages.geojson.layer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.geojson.GeoJSONFeature
import com.mapconductor.geojson.GeoJSONLayer
import com.mapconductor.geojson.GeoJSONLayerState
import com.mapconductor.utils.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val GEOJSON_ASSET = "N02-22_GML.zip"

/**
 * 国土数値情報の鉄道データ（N02）の属性名。
 *
 * 生の `N02_001` のままだと何の値か分からないので、吹き出しでは名前に置き換える。
 * react / ios と**同じ文言**にしてある（3 プラットフォームを並べて見比べるサンプルなので、
 * ここが違うと同じ地物を選んでいるのか判断できない）。
 *
 * ここに無いキーは生のキー名をそのまま出す。データ側に属性が増えても表から消えないように。
 */
private val PROPERTY_LABELS =
    mapOf(
        "N02_001" to ("鉄道区分" to "Railway category"),
        "N02_002" to ("事業者区分" to "Business category"),
        "N02_003" to ("路線名" to "Railway name"),
        "N02_004" to ("運営会社" to "Railway company"),
    )

/**
 * 値の英語表記が入っている属性の接尾辞。
 *
 * geojson 側が `N02_003`（路線名）に対して `N02_003_en` を持っている。アプリに
 * 対訳表を置くと 4 プラットフォーム分そろえる羽目になるので、データに持たせてある。
 */
private const val ENGLISH_SUFFIX = "_en"

@Composable
fun GeoJSONLayerMapPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = GeoPoint(35.68, 139.77),
                zoom = 13.0,
            )
        }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { mapViewState = it },
    ) { paddingValues ->
        GeoJSONLayerMapComponent(mapViewState = mapViewState)

        MessageCard(
            title = "GeoJSON Layer",
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            Text("Tap a feature to inspect its properties.")
        }
    }
}

@Composable
private fun GeoJSONLayerMapComponent(mapViewState: MapViewStateInterface<*>?) {
    val context = LocalContext.current
    var selectedFeature by remember { mutableStateOf<GeoJSONFeature?>(null) }
    var tappedPosition by remember { mutableStateOf<GeoPoint?>(null) }
    var features by remember { mutableStateOf<List<GeoJSONFeature>>(emptyList()) }
    var isDataLoading by remember { mutableStateOf(true) }

    val layerState =
        remember {
            GeoJSONLayerState(
                // Fallback style properties
                strokeColor = android.graphics.Color.argb(127, 250, 36, 29),
                strokeWidth = 3f,
                pointRadius = 6f,
                onLoadStart = { isDataLoading = true },
                onLoadComplete = { _ -> isDataLoading = false },
                onClick = { feature, position ->
                    selectedFeature = feature
                    tappedPosition = GeoPoint.from(position)
                },
            )
        }

    LaunchedEffect(Unit) {
        try {
            layerState.onLoadStart?.invoke()
            // Try to create ExampleGeoJSONLayerLoader for the N02-22_RailroadSection.style.json
            val data =
                withContext(Dispatchers.IO) {
                    ExampleGeoJSONLayerLoader(context.assets).load(GEOJSON_ASSET)
                }
            layerState.styleProvider = data.styleProvider
            features = data.features
            layerState.onLoadComplete?.invoke(null)
        } catch (e: Throwable) {
            layerState.onLoadComplete?.invoke(e)
        } finally {
        }
    }

    mapViewState?.let { state ->
        MapViewContainer(
            state = state,
            onMapClick = { clicked ->
                if (!layerState.processClick(clicked, pixelTolerance = 10.0, zoom = state.cameraPosition.zoom)) {
                    tappedPosition = null
                    selectedFeature = null
                }
            },
        ) {
            GeoJSONLayer(state = layerState, features = features)

            tappedPosition?.let { position ->
                InfoBubble(position = position) {
                    selectedFeature?.properties?.let { properties ->
                        PropertyTable(properties)
                    }
                }
            }
        }
    }

    if (isDataLoading) {
        LoadingDialog(
            title = "Loading GeoJSON",
            message = "Parsing $GEOJSON_ASSET...",
        )
    }
}

/**
 * 端末の言語が日本語なら日本語、それ以外は英語で出す。
 *
 * 英語のときは値も `N02_003_en` の側へ差し替える。`_en` の行そのものは出さない
 * （同じ項目が 2 行に増えてしまうため）。
 */
@Composable
private fun PropertyTable(properties: Map<String, Any?>) {
    val isJapanese = LocalConfiguration.current.locales[0].language == "ja"

    Column(
        modifier =
            Modifier
                .width(400.dp)
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.background(Color(0xFFE0E0E0))) {
            PropertyTableCell(text = if (isJapanese) "プロパティ" else "Property", weight = 0.5f)
            PropertyTableCell(text = if (isJapanese) "値" else "Value", weight = 0.5f)
        }

        properties.forEach { (key, value) ->
            if (key.endsWith(ENGLISH_SUFFIX)) return@forEach
            val label = PROPERTY_LABELS[key]?.let { if (isJapanese) it.first else it.second } ?: key
            val shown = if (isJapanese) value else properties[key + ENGLISH_SUFFIX] ?: value
            Row(modifier = Modifier.fillMaxWidth()) {
                PropertyTableCell(text = label, weight = 0.5f)
                PropertyTableCell(text = shown?.toString().orEmpty(), weight = 0.5f)
            }
        }
    }
}

@Composable
private fun RowScope.PropertyTableCell(
    text: String,
    weight: Float,
) {
    Text(
        text = text,
        color = Color.Black,
        modifier =
            Modifier
                .border(1.dp, Color.Gray)
                .weight(weight)
                .padding(8.dp),
    )
}
