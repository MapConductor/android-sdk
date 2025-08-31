package com.mapconductor.example.pages.marker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.example.MapViewContainer

@Composable
fun MarkerBasicMapComponent(
    mapViewState: MapViewState<*>,
    modifier: Modifier = Modifier,
) {
    MapViewContainer(
        state = mapViewState,
        modifier = modifier,
    ) {
//        Marker(
//            position = GeoPoint.fromLatLong(0.0, 0.004),
//        )
//        Marker(
//            position = GeoPoint.fromLatLong(0.0, 0.008),
//            icon = DefaultIcon(
//                fillColor = Color.Yellow,
//                strokeColor = Color.Black,
//                strokeWidth = 2.dp,
//            ),
//        )
//        Marker(
//            position = GeoPoint.fromLatLong(0.0, 0.012),
//            icon = DefaultIcon(
//                label = "AB",
//                labelTextColor = Color.White,
//                labelStrokeColor = Color.Black,
//            ),
//        )

        Marker(
            position = GeoPoint.fromLatLong(-0.004, 0.004),
            icon =
                DefaultIcon(
                    scale = 0.7f,
                ),
        )

//        Marker(
//            position = GeoPoint.fromLatLong(-0.004, 0.008),
//            icon = DefaultIcon(
//                scale = 1.4f,
//            ),
//        )
//
//        Marker(
//            position = GeoPoint.fromLatLong(-0.004, 0.012),
//            icon = DefaultIcon(
//                scale = 2.1f,
//            ),
//        )
    }
}
