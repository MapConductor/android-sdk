package com.mapconductor.example.pages.geojson.basic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.geojson.GeoJSONFeature
import com.mapconductor.geojson.GeoJSONLayer
import com.mapconductor.geojson.GeoJSONLayerState
import com.mapconductor.geojson.GeoJSONParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BasicGeoJSONMapPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = GeoPoint.fromLongLat(55.3089185, 25.255377),
                zoom = 13.0,
            )
        }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { mapViewState = it },
    ) {
        BasicGeoJSONMapComponent(mapViewState = mapViewState)
    }
}

@Composable
private fun BasicGeoJSONMapComponent(mapViewState: MapViewStateInterface<*>?) {
    val layerState =
        remember {
            GeoJSONLayerState(
                fillColor = android.graphics.Color.argb(127, 0x3b, 0xb2, 0xd0),
                strokeColor = android.graphics.Color.argb(255, 0x1d, 0x70, 0x82),
                strokeWidth = 2f,
            )
        }
    var features by remember { mutableStateOf<List<GeoJSONFeature>>(emptyList()) }

    LaunchedEffect(Unit) {
        features =
            withContext(Dispatchers.IO) {
                BASIC_GEOJSON.byteInputStream(Charsets.UTF_8).use(GeoJSONParser::parseStream)
            }
    }

    mapViewState?.let { state ->
        MapViewContainer(
            state = state,
        ) {
            GeoJSONLayer(state = layerState, features = features)
        }
    }
}

private val BASIC_GEOJSON =
    """
    {
      "type": "FeatureCollection",
      "features": [
        {
          "type": "Feature",
          "geometry": {
            "type": "Polygon",
            "coordinates": [
              [
                [55.30122473231012, 25.26476622289597],
                [55.29743486255916, 25.25827212207261],
                [55.28978863411328, 25.251356725509737],
                [55.300027931336984, 25.246425506635504],
                [55.307474692951274, 25.244200378933655],
                [55.31212891895635, 25.256408010450187],
                [55.30774064871093, 25.26266169122738],
                [55.301357710197806, 25.264946609615492],
                [55.30122473231012, 25.26476622289597]
              ],
              [
                [55.30084858315658, 25.256531695820797],
                [55.298280197635705, 25.252243254705405],
                [55.30163885563897, 25.250501032248863],
                [55.304059065092645, 25.254700192612702],
                [55.30084858315658, 25.256531695820797]
              ],
              [
                [55.30173763969924, 25.262517391695198],
                [55.301095543307355, 25.26122200491396],
                [55.30396028103232, 25.259479911263526],
                [55.30489872958182, 25.261132667394975],
                [55.30173763969924, 25.262517391695198]
              ]
            ]
          },
          "properties": {}
        }
      ]
    }
    """.trimIndent()
