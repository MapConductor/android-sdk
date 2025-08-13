package com.mapconductor.example.pages.map.basic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polyline.PolylineState
import android.util.Log

interface BasicMapPageViewModel {

    val polylines: List<PolylineState>
    var geodesic: Boolean
}

class BasicMapPageViewModelImpl : ViewModel(),
    BasicMapPageViewModel {

    private val honoluluLocation = GeoPoint.fromLatLong(21.3099, -157.8581)
    private val tokyoLocation = GeoPoint.fromLatLong(35.6762, 139.6503)
    private val londonLocation = GeoPoint.fromLatLong(51.5074, -0.1278)

    override var geodesic by mutableStateOf(false)

    override val polylines: List<PolylineState> = listOf(
        PolylineState(
            id = "honolulu_to_tokyo",
            points = listOf(honoluluLocation, tokyoLocation),
            strokeColor = Color.Blue.copy(alpha = 0.7f),
            strokeWidth = 3.dp,
            geodesic = geodesic,
        ),
        PolylineState(
            id = "honolulu_to_london",
            points = listOf(honoluluLocation, londonLocation),
            strokeColor = Color.Blue.copy(alpha = 0.7f),
            strokeWidth = 3.dp,
            geodesic = geodesic,
        ),
    )
}
