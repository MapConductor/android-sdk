package com.mapconductor.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.toFixed

@Composable
fun BoxScope.MapViewStatePanel(camera: MapCameraPosition?) {
    Column(
        modifier =
            Modifier
                .align(Alignment.TopEnd)
                .background(
                    Color(
                        red = 0.9f,
                        green = 0.9f,
                        blue = 0.9f,
                        alpha = 0.75f,
                    ),
                ).wrapContentHeight()
                .fillMaxWidth(),
    ) {
        Text("LatLng: (${camera?.position?.toUrlValue()})", color = Color.Black)
        Text("Zoom: ${camera?.zoom?.toFixed(2)}", color = Color.Black)
        Text("bearing: ${camera?.bearing?.toInt()}", color = Color.Black)
        Text("tilt: ${camera?.tilt?.toInt()}", color = Color.Black)
    }
}
